package dev.hexagonal101.library.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * The lending rules of the library.
 * <p>
 * No I/O and no framework: everything it needs is passed in, including today's date.
 * It does not know where members, books or loans come from, nor who asked.
 * That is the job of the application layer.
 */
public final class LoanPolicy {

    /**
     * @param member      the member asking for the book
     * @param book        the requested book
     * @param bookOnLoan  whether someone already holds the book
     * @param memberLoans the loans the member currently holds
     * @param today       the current date
     */
    public LoanDecision decide(Member member, Book book, boolean bookOnLoan, List<Loan> memberLoans, LocalDate today) {
        if (bookOnLoan) {
            return new LoanDecision.Refused(RefusalReason.BOOK_ALREADY_ON_LOAN);
        }
        if (memberLoans.size() >= member.tier().maxActiveLoans()) {
            return new LoanDecision.Refused(RefusalReason.LOAN_QUOTA_REACHED);
        }
        if (memberLoans.stream().anyMatch(loan -> loan.isOverdue(today))) {
            return new LoanDecision.Refused(RefusalReason.OVERDUE_LOANS);
        }

        LocalDate dueOn = today.plus(member.tier().loanDuration());
        return new LoanDecision.Granted(new Loan(LoanId.newId(), book.id(), member.id(), today, dueOn));
    }
}
