package dev.hexagonal101.library.application.port.in;

import java.time.LocalDate;

/**
 * The output of {@link BorrowBookUseCase}, as seen from outside the hexagon.
 * <p>
 * The domain has its own {@code LoanDecision}; it stays inside. Adapters receive this
 * result type, owned by the port, so the domain model can change without breaking them.
 */
public sealed interface BorrowBookResult {

    record Granted(String loanId, String memberId, String bookId, LocalDate borrowedOn, LocalDate dueOn)
            implements BorrowBookResult {
    }

    record Refused(Reason reason) implements BorrowBookResult {
    }

    /** Why the loan was refused. */
    enum Reason {
        BOOK_ALREADY_ON_LOAN,
        LOAN_QUOTA_REACHED,
        OVERDUE_LOANS
    }
}
