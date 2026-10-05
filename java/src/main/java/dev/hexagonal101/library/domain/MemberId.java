package dev.hexagonal101.library.domain;

import java.util.Objects;

/** Identifies a library member. A value object: two ids with the same value are equal. */
public record MemberId(String value) {

    public MemberId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("MemberId must not be blank");
        }
    }
}
