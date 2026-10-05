package dev.hexagonal101.library.adapter.in.kafka;

import org.jspecify.annotations.Nullable;

import dev.hexagonal101.library.application.port.in.BorrowBookCommand;

/**
 * A message of the {@code library.loan-requests} topic.
 * {@code requestId} is chosen by the sender and echoed in the result, to match answers to questions.
 */
record LoanRequestMessage(@Nullable String requestId, @Nullable String memberId, @Nullable String bookId) {

    BorrowBookCommand toCommand() {
        return new BorrowBookCommand(memberId, bookId);
    }
}
