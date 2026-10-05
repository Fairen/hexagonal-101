package dev.hexagonal101.library.application.port.out;

import java.util.List;

import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.MemberId;

/**
 * Driven port: where ongoing loans are stored. Implemented by the S3 or the SQLite adapter.
 * <p>
 * Notice the vocabulary: "loans of a member", "is this book on loan".
 * Not "SELECT", not "GetObject". The port speaks the language of the use case.
 */
public interface LoanRepository {

    /** The loans the member currently holds. */
    List<Loan> findActiveByMember(MemberId memberId);

    boolean isBookOnLoan(BookId bookId);

    void save(Loan loan);
}
