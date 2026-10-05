package dev.hexagonal101.library.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import dev.hexagonal101.library.application.port.in.BookNotFoundException;
import dev.hexagonal101.library.application.port.in.BorrowBookCommand;
import dev.hexagonal101.library.application.port.in.BorrowBookResult;
import dev.hexagonal101.library.application.port.in.MemberNotFoundException;
import dev.hexagonal101.library.application.port.out.BookCatalog;
import dev.hexagonal101.library.application.port.out.LoanRepository;
import dev.hexagonal101.library.application.port.out.MemberDirectory;
import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.LoanPolicy;
import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.MembershipTier;

/**
 * The use case is tested with in-memory fakes of its driven ports:
 * no S3, no database, no Spring context. This is what the ports buy us.
 */
class BorrowBookServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-02T10:00:00Z"), ZoneOffset.UTC);

    private final InMemoryLibrary library = new InMemoryLibrary();
    private final BorrowBookService service =
            new BorrowBookService(library, library, library, new LoanPolicy(), CLOCK);

    private final Member ada = new Member(new MemberId("m-1"), "Ada", MembershipTier.STANDARD);
    private final Book ddd = new Book(new BookId("b-1"), "Domain-Driven Design", "Eric Evans");

    @Test
    void stores_the_loan_when_granted() {
        library.add(ada);
        library.add(ddd);

        BorrowBookResult result = service.borrow(new BorrowBookCommand("m-1", "b-1"));

        assertThat(result).isInstanceOfSatisfying(BorrowBookResult.Granted.class, granted -> {
            assertThat(granted.bookId()).isEqualTo("b-1");
            assertThat(granted.dueOn()).isEqualTo(LocalDate.of(2026, 10, 16));
        });
        assertThat(library.loans).singleElement().satisfies(loan -> {
            assertThat(loan.bookId()).isEqualTo(ddd.id());
            assertThat(loan.borrowedOn()).isEqualTo(LocalDate.of(2026, 10, 2));
        });
    }

    @Test
    void stores_nothing_when_refused() {
        library.add(ada);
        library.add(ddd);
        service.borrow(new BorrowBookCommand("m-1", "b-1"));

        BorrowBookResult second = service.borrow(new BorrowBookCommand("m-1", "b-1"));

        assertThat(second).isEqualTo(new BorrowBookResult.Refused(BorrowBookResult.Reason.BOOK_ALREADY_ON_LOAN));
        assertThat(library.loans).hasSize(1);
    }

    @Test
    void the_command_requires_both_ids() {
        assertThatThrownBy(() -> new BorrowBookCommand("m-1", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_an_unknown_member() {
        library.add(ddd);

        assertThatThrownBy(() -> service.borrow(new BorrowBookCommand("nobody", "b-1")))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void rejects_an_unknown_book() {
        library.add(ada);

        assertThatThrownBy(() -> service.borrow(new BorrowBookCommand("m-1", "nothing")))
                .isInstanceOf(BookNotFoundException.class);
    }

    /** One fake implementing the three driven ports. */
    private static final class InMemoryLibrary implements MemberDirectory, BookCatalog, LoanRepository {

        private final Map<MemberId, Member> members = new HashMap<>();
        private final Map<BookId, Book> books = new HashMap<>();
        private final List<Loan> loans = new ArrayList<>();

        void add(Member member) {
            members.put(member.id(), member);
        }

        void add(Book book) {
            books.put(book.id(), book);
        }

        @Override
        public Optional<Member> findById(MemberId id) {
            return Optional.ofNullable(members.get(id));
        }

        @Override
        public Optional<Book> findById(BookId id) {
            return Optional.ofNullable(books.get(id));
        }

        @Override
        public List<Loan> findActiveByMember(MemberId memberId) {
            return loans.stream().filter(loan -> loan.memberId().equals(memberId)).toList();
        }

        @Override
        public boolean isBookOnLoan(BookId bookId) {
            return loans.stream().anyMatch(loan -> loan.bookId().equals(bookId));
        }

        @Override
        public void save(Loan loan) {
            loans.add(loan);
        }
    }
}
