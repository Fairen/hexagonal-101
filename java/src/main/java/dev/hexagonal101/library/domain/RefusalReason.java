package dev.hexagonal101.library.domain;

/** Why the library said no. */
public enum RefusalReason {

    /** Someone else already holds this book. */
    BOOK_ALREADY_ON_LOAN,

    /** The member already holds as many books as their tier allows. */
    LOAN_QUOTA_REACHED,

    /** The member must first return the books they are late with. */
    OVERDUE_LOANS
}
