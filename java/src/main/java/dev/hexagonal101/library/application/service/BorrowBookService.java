package dev.hexagonal101.library.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import dev.hexagonal101.library.application.port.in.BookNotFoundException;
import dev.hexagonal101.library.application.port.in.BorrowBookCommand;
import dev.hexagonal101.library.application.port.in.BorrowBookResult;
import dev.hexagonal101.library.application.port.in.BorrowBookUseCase;
import dev.hexagonal101.library.application.port.in.MemberNotFoundException;
import dev.hexagonal101.library.application.port.out.BookCatalog;
import dev.hexagonal101.library.application.port.out.LoanRepository;
import dev.hexagonal101.library.application.port.out.MemberDirectory;
import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanDecision;
import dev.hexagonal101.library.domain.LoanPolicy;
import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.RefusalReason;

/**
 * Orchestrates a loan: gather the facts through the driven ports,
 * let the domain decide, persist the outcome, answer in the terms of the driving port.
 * <p>
 * Plain Java: no Spring annotation. The bootstrap package hands it to Spring,
 * so it does not know whether it runs behind REST or Kafka, on S3 or SQLite.
 */
public final class BorrowBookService implements BorrowBookUseCase {

    private final MemberDirectory members;
    private final BookCatalog books;
    private final LoanRepository loans;
    private final LoanPolicy policy;
    private final Clock clock;

    public BorrowBookService(MemberDirectory members, BookCatalog books, LoanRepository loans,
                             LoanPolicy policy, Clock clock) {
        this.members = members;
        this.books = books;
        this.loans = loans;
        this.policy = policy;
        this.clock = clock;
    }

    @Override
    public BorrowBookResult borrow(BorrowBookCommand command) {
        Member member = members.findById(new MemberId(command.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));
        Book book = books.findById(new BookId(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        boolean bookOnLoan = loans.isBookOnLoan(book.id());
        List<Loan> memberLoans = loans.findActiveByMember(member.id());

        LoanDecision decision = policy.decide(member, book, bookOnLoan, memberLoans, LocalDate.now(clock));

        return switch (decision) {
            case LoanDecision.Granted(Loan loan) -> {
                loans.save(loan);
                yield new BorrowBookResult.Granted(loan.id().value(), loan.memberId().value(),
                        loan.bookId().value(), loan.borrowedOn(), loan.dueOn());
            }
            case LoanDecision.Refused(RefusalReason reason) -> new BorrowBookResult.Refused(switch (reason) {
                case BOOK_ALREADY_ON_LOAN -> BorrowBookResult.Reason.BOOK_ALREADY_ON_LOAN;
                case LOAN_QUOTA_REACHED -> BorrowBookResult.Reason.LOAN_QUOTA_REACHED;
                case OVERDUE_LOANS -> BorrowBookResult.Reason.OVERDUE_LOANS;
            });
        };
    }
}
