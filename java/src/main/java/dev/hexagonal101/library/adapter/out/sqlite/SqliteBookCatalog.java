package dev.hexagonal101.library.adapter.out.sqlite;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

import dev.hexagonal101.library.application.port.out.BookCatalog;
import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;

/** {@link BookCatalog} port, implemented with SQL on the {@code book} table. */
class SqliteBookCatalog implements BookCatalog {

    private final JdbcClient jdbc;

    SqliteBookCatalog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return jdbc.sql("SELECT id, title, author FROM book WHERE id = :id")
                .param("id", id.value())
                .query((row, rowNum) -> new Book(
                        new BookId(row.getString("id")), row.getString("title"), row.getString("author")))
                .optional();
    }
}
