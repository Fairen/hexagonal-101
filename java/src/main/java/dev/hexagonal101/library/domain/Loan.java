package dev.hexagonal101.library.domain;

import java.time.LocalDate;
import java.util.Objects;

/** A book currently held by a member, to be returned by {@code dueOn}. */
public record Loan(LoanId id, BookId bookId, MemberId memberId, LocalDate borrowedOn, LocalDate dueOn) {

    public Loan {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(bookId, "bookId");
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(borrowedOn, "borrowedOn");
        Objects.requireNonNull(dueOn, "dueOn");
        if (dueOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("A loan cannot be due before it starts");
        }
    }

    public boolean isOverdue(LocalDate today) {
        return today.isAfter(dueOn);
    }
}
