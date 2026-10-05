package dev.hexagonal101.library.application.port.out;

import java.util.Optional;

import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;

/** Driven port: where books are looked up. Implemented by the S3 or the SQLite adapter. */
public interface BookCatalog {

    Optional<Book> findById(BookId id);
}
