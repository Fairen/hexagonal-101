package dev.hexagonal101.library.domain;

import java.util.Objects;

/** Identifies a book of the catalog. */
public record BookId(String value) {

    public BookId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("BookId must not be blank");
        }
    }
}
