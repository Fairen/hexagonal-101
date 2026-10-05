package dev.hexagonal101.library.adapter.in.kafka;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import dev.hexagonal101.library.application.port.in.BorrowBookResult;

/** A message of the {@code library.loan-results} topic: the Kafka equivalent of the HTTP response. */
record LoanResultMessage(@Nullable String requestId, Status status, @Nullable String loanId,
                         @Nullable LocalDate dueOn, @Nullable String reason) {

    enum Status { GRANTED, REFUSED, NOT_FOUND, INVALID }

    static LoanResultMessage of(@Nullable String requestId, BorrowBookResult result) {
        return switch (result) {
            case BorrowBookResult.Granted granted ->
                    new LoanResultMessage(requestId, Status.GRANTED, granted.loanId(), granted.dueOn(), null);
            case BorrowBookResult.Refused(BorrowBookResult.Reason reason) ->
                    new LoanResultMessage(requestId, Status.REFUSED, null, null, reason.name());
        };
    }

    static LoanResultMessage failed(@Nullable String requestId, Status status, @Nullable String reason) {
        return new LoanResultMessage(requestId, status, null, null, reason);
    }
}
