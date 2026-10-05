package dev.hexagonal101.library.adapter.out.sqlite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.LocalDate;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.sqlite.SQLiteDataSource;

import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanId;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.MembershipTier;

/** The SQLite adapter against a real SQLite file, migrated by Flyway. */
class SqliteAdapterTest {

    @TempDir
    Path tempDir;

    private JdbcClient jdbc;

    @BeforeEach
    void setUp() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tempDir.resolve("library.db"));
        Flyway.configure().dataSource(dataSource).locations("classpath:db/sqlite").load().migrate();
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void reads_the_seeded_library() {
        assertThat(new SqliteBookCatalog(jdbc).findById(new BookId("b-1")))
                .hasValueSatisfying(book -> assertThat(book.title()).isEqualTo("Domain-Driven Design"));
        assertThat(new SqliteMemberDirectory(jdbc).findById(new MemberId("m-3")))
                .hasValueSatisfying(member -> assertThat(member.tier()).isEqualTo(MembershipTier.PREMIUM));
        assertThat(new SqliteLoanRepository(jdbc).findActiveByMember(new MemberId("m-2"))).hasSize(3);
        assertThat(new SqliteLoanRepository(jdbc).isBookOnLoan(new BookId("b-2"))).isTrue();
        assertThat(new SqliteBookCatalog(jdbc).findById(new BookId("nope"))).isEmpty();
    }

    @Test
    void saves_a_loan() {
        SqliteLoanRepository loans = new SqliteLoanRepository(jdbc);

        loans.save(loan("b-1", "m-1"));

        assertThat(loans.isBookOnLoan(new BookId("b-1"))).isTrue();
        assertThat(loans.findActiveByMember(new MemberId("m-1")))
                .singleElement()
                .satisfies(loan -> assertThat(loan.dueOn()).isEqualTo(LocalDate.of(2026, 10, 16)));
    }

    @Test
    void the_database_refuses_a_second_loan_of_the_same_book() {
        SqliteLoanRepository loans = new SqliteLoanRepository(jdbc);

        assertThatThrownBy(() -> loans.save(loan("b-2", "m-1")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("UNIQUE constraint failed: loan.book_id");
    }

    private static Loan loan(String bookId, String memberId) {
        LocalDate today = LocalDate.of(2026, 10, 2);
        return new Loan(LoanId.newId(), new BookId(bookId), new MemberId(memberId), today, today.plusDays(14));
    }
}
