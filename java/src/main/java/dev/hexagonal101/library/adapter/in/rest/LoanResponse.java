package dev.hexagonal101.library.adapter.in.rest;

import java.time.LocalDate;

import dev.hexagonal101.library.application.port.in.BorrowBookResult;

/** The JSON body returned when a loan is granted. */
record LoanResponse(String loanId, String memberId, String bookId, LocalDate borrowedOn, LocalDate dueOn) {

    static LoanResponse from(BorrowBookResult.Granted granted) {
        return new LoanResponse(granted.loanId(), granted.memberId(), granted.bookId(),
                granted.borrowedOn(), granted.dueOn());
    }
}
