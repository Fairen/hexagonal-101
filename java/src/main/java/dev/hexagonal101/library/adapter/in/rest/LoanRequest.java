package dev.hexagonal101.library.adapter.in.rest;

import org.jspecify.annotations.Nullable;

import dev.hexagonal101.library.application.port.in.BorrowBookCommand;

/** The JSON body of {@code POST /loans}. An HTTP detail: the use case only sees the command. */
record LoanRequest(@Nullable String memberId, @Nullable String bookId) {

    BorrowBookCommand toCommand() {
        return new BorrowBookCommand(memberId, bookId);
    }
}
