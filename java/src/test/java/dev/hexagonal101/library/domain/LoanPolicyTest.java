package dev.hexagonal101.library.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class LoanPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 2);

    private final LoanPolicy policy = new LoanPolicy();

    private final Book book = new Book(new BookId("b-1"), "Domain-Driven Design", "Eric Evans");
    private final Member standard = new Member(new MemberId("m-1"), "Ada", MembershipTier.STANDARD);
    private final Member premium = new Member(new MemberId("m-2"), "Grace", MembershipTier.PREMIUM);

    @Test
    void grants_a_loan_due_after_the_tier_duration() {
        LoanDecision decision = policy.decide(standard, book, false, List.of(), TODAY);

        assertThat(decision).isInstanceOfSatisfying(LoanDecision.Granted.class, granted -> {
            assertThat(granted.loan().bookId()).isEqualTo(book.id());
            assertThat(granted.loan().memberId()).isEqualTo(standard.id());
            assertThat(granted.loan().borrowedOn()).isEqualTo(TODAY);
            assertThat(granted.loan().dueOn()).isEqualTo(TODAY.plusDays(14));
        });
    }

    @Test
    void premium_members_keep_books_longer() {
        LoanDecision decision = policy.decide(premium, book, false, List.of(), TODAY);

        assertThat(decision).isInstanceOfSatisfying(LoanDecision.Granted.class,
                granted -> assertThat(granted.loan().dueOn()).isEqualTo(TODAY.plusDays(28)));
    }

    @Test
    void refuses_a_book_already_on_loan() {
        LoanDecision decision = policy.decide(standard, book, true, List.of(), TODAY);

        assertThat(decision).isEqualTo(new LoanDecision.Refused(RefusalReason.BOOK_ALREADY_ON_LOAN));
    }

    @Test
    void refuses_when_the_quota_of_the_tier_is_reached() {
        List<Loan> threeLoans = loansOf(standard, 3, TODAY);

        LoanDecision decision = policy.decide(standard, book, false, threeLoans, TODAY);

        assertThat(decision).isEqualTo(new LoanDecision.Refused(RefusalReason.LOAN_QUOTA_REACHED));
    }

    @Test
    void premium_quota_is_higher() {
        List<Loan> threeLoans = loansOf(premium, 3, TODAY);

        LoanDecision decision = policy.decide(premium, book, false, threeLoans, TODAY);

        assertThat(decision).isInstanceOf(LoanDecision.Granted.class);
    }

    @Test
    void refuses_while_a_loan_is_overdue() {
        Loan overdue = new Loan(LoanId.newId(), new BookId("b-9"), standard.id(),
                TODAY.minusDays(30), TODAY.minusDays(16));

        LoanDecision decision = policy.decide(standard, book, false, List.of(overdue), TODAY);

        assertThat(decision).isEqualTo(new LoanDecision.Refused(RefusalReason.OVERDUE_LOANS));
    }

    @Test
    void a_loan_due_today_is_not_overdue() {
        Loan dueToday = new Loan(LoanId.newId(), new BookId("b-9"), standard.id(), TODAY.minusDays(14), TODAY);

        LoanDecision decision = policy.decide(standard, book, false, List.of(dueToday), TODAY);

        assertThat(decision).isInstanceOf(LoanDecision.Granted.class);
    }

    private static List<Loan> loansOf(Member member, int count, LocalDate borrowedOn) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Loan(LoanId.newId(), new BookId("other-" + i), member.id(),
                        borrowedOn, borrowedOn.plus(member.tier().loanDuration())))
                .toList();
    }
}
