package dev.hexagonal101.library.adapter.out.s3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanId;
import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.MembershipTier;

/**
 * The layout of library.json. It is a storage format, deliberately separate from the domain model:
 * renaming a JSON field must never force a change in the domain, and vice versa.
 */
record LibraryJson(List<BookJson> books, List<MemberJson> members, List<LoanJson> loans) {

    LibraryJson withLoan(Loan loan) {
        List<LoanJson> updated = new ArrayList<>(loans);
        updated.add(LoanJson.from(loan));
        return new LibraryJson(books, members, updated);
    }

    record BookJson(String id, String title, String author) {
        Book toDomain() {
            return new Book(new BookId(id), title, author);
        }
    }

    record MemberJson(String id, String name, String tier) {
        Member toDomain() {
            return new Member(new MemberId(id), name, MembershipTier.valueOf(tier));
        }
    }

    record LoanJson(String id, String bookId, String memberId, LocalDate borrowedOn, LocalDate dueOn) {
        static LoanJson from(Loan loan) {
            return new LoanJson(loan.id().value(), loan.bookId().value(), loan.memberId().value(),
                    loan.borrowedOn(), loan.dueOn());
        }

        Loan toDomain() {
            return new Loan(new LoanId(id), new BookId(bookId), new MemberId(memberId), borrowedOn, dueOn);
        }
    }
}
