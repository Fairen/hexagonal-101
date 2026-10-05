package dev.hexagonal101.library.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * The whole hexagon, REST in and SQLite out, without any external infrastructure.
 * Also proves that the Kafka and S3 adapters stay asleep when they are not selected.
 */
@SpringBootTest(properties = {
        "library.adapters.inbound=rest",
        "library.adapters.outbound=sqlite"
})
@AutoConfigureMockMvc
class LibraryApplicationTest {

    @DynamicPropertySource
    static void sqliteFile(DynamicPropertyRegistry registry) throws IOException {
        String file = Files.createTempFile("library-", ".db").toString();
        registry.add("library.sqlite.url", () -> "jdbc:sqlite:" + file);
    }

    @Autowired
    private MockMvcTester mvc;

    @Test
    void borrows_a_book_end_to_end() {
        assertThat(borrow("m-1", "b-1")).hasStatus(HttpStatus.CREATED);
        assertThat(borrow("m-1", "b-1")).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.reason").isEqualTo("BOOK_ALREADY_ON_LOAN");
    }

    @Test
    void applies_the_lending_rules_on_the_seeded_members() {
        assertThat(borrow("m-2", "b-6")).bodyJson().extractingPath("$.reason").isEqualTo("LOAN_QUOTA_REACHED");
        assertThat(borrow("m-3", "b-7")).bodyJson().extractingPath("$.reason").isEqualTo("OVERDUE_LOANS");
        assertThat(borrow("m-9", "b-7")).hasStatus(HttpStatus.NOT_FOUND);
    }

    private MockMvcTester.MockMvcRequestBuilder borrow(String memberId, String bookId) {
        return mvc.post().uri("/loans").contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"%s\",\"bookId\":\"%s\"}".formatted(memberId, bookId));
    }
}
