package dev.hexagonal101.library.domain;

import java.util.Objects;
import java.util.UUID;

/** Identifies a loan. New loans get a random id. */
public record LoanId(String value) {

    public LoanId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("LoanId must not be blank");
        }
    }

    public static LoanId newId() {
        return new LoanId(UUID.randomUUID().toString());
    }
}
