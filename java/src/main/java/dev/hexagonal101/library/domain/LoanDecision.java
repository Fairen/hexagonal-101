package dev.hexagonal101.library.domain;

/**
 * The outcome of a loan request.
 * <p>
 * A refusal is a normal business outcome, not an error: it is modelled as data
 * rather than as an exception. The interface is sealed, so the use case handles both
 * cases with an exhaustive {@code switch} and the compiler tells it if one is forgotten.
 * It never leaves the hexagon: the driving port translates it into its own result type.
 */
public sealed interface LoanDecision {

    record Granted(Loan loan) implements LoanDecision {
    }

    record Refused(RefusalReason reason) implements LoanDecision {
    }
}
