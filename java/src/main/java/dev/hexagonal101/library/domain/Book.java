package dev.hexagonal101.library.domain;

import java.util.Objects;

/** A book of the catalog. */
public record Book(BookId id, String title, String author) {

    public Book {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(author, "author");
    }
}
