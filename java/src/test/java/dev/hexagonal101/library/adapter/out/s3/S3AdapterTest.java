package dev.hexagonal101.library.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanId;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.MembershipTier;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.json.JsonMapper;

/** The S3 adapter against a real S3-compatible server (Adobe S3Mock). */
@Testcontainers
class S3AdapterTest {

    @Container
    static GenericContainer<?> s3mock = new GenericContainer<>("adobe/s3mock:5.2.3").withExposedPorts(9090);

    private S3LibraryDocument document;

    @BeforeEach
    void setUp() throws IOException {
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://" + s3mock.getHost() + ":" + s3mock.getMappedPort(9090)))
                .forcePathStyle(true)
                .region(Region.US_EAST_1)
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
        document = new S3LibraryDocument(s3, JsonMapper.builder().build(), "library", "test-" + UUID.randomUUID() + ".json");
        document.createIfMissing(seed());
    }

    @Test
    void reads_the_seeded_library() {
        assertThat(new S3BookCatalog(document).findById(new BookId("b-1")))
                .hasValueSatisfying(book -> assertThat(book.title()).isEqualTo("Domain-Driven Design"));
        assertThat(new S3MemberDirectory(document).findById(new MemberId("m-3")))
                .hasValueSatisfying(member -> assertThat(member.tier()).isEqualTo(MembershipTier.PREMIUM));
        assertThat(new S3LoanRepository(document).findActiveByMember(new MemberId("m-2"))).hasSize(3);
        assertThat(new S3LoanRepository(document).isBookOnLoan(new BookId("b-2"))).isTrue();
        assertThat(new S3BookCatalog(document).findById(new BookId("nope"))).isEmpty();
    }

    @Test
    void saves_a_loan() {
        S3LoanRepository loans = new S3LoanRepository(document);

        loans.save(loan("b-1", "m-1"));

        assertThat(loans.isBookOnLoan(new BookId("b-1"))).isTrue();
        assertThat(loans.findActiveByMember(new MemberId("m-1"))).hasSize(1);
    }

    @Test
    void seeding_never_overwrites_an_existing_document() throws IOException {
        new S3LoanRepository(document).save(loan("b-1", "m-1"));

        document.createIfMissing(seed());

        assertThat(new S3LoanRepository(document).isBookOnLoan(new BookId("b-1"))).isTrue();
    }

    @Test
    void a_concurrent_write_is_not_lost() {
        boolean[] firstAttempt = {true};

        document.update(library -> {
            if (firstAttempt[0]) {
                firstAttempt[0] = false;
                // Another writer sneaks in between our read and our write: the ETag changes.
                new S3LoanRepository(document).save(loan("b-6", "m-1"));
            }
            return library.withLoan(loan("b-7", "m-1"));
        });

        S3LoanRepository loans = new S3LoanRepository(document);
        assertThat(loans.isBookOnLoan(new BookId("b-6"))).as("the other writer's loan").isTrue();
        assertThat(loans.isBookOnLoan(new BookId("b-7"))).as("our loan, written on retry").isTrue();
    }

    private static Loan loan(String bookId, String memberId) {
        LocalDate today = LocalDate.of(2026, 10, 2);
        return new Loan(LoanId.newId(), new BookId(bookId), new MemberId(memberId), today, today.plusDays(14));
    }

    private static byte[] seed() throws IOException {
        try (InputStream in = S3AdapterTest.class.getResourceAsStream("/s3/library-seed.json")) {
            return in.readAllBytes();
        }
    }
}
