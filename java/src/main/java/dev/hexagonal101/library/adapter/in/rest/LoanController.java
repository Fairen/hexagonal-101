package dev.hexagonal101.library.adapter.in.rest;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.hexagonal101.library.application.port.in.BorrowBookResult;
import dev.hexagonal101.library.application.port.in.BorrowBookUseCase;

/**
 * {@code POST /loans}: HTTP in, {@link BorrowBookUseCase} call, HTTP out.
 * <p>
 * The controller only knows the driving port: its interface, its command, its result.
 * Not the service behind it, not the domain model.
 */
@RestController
@RequestMapping("/loans")
class LoanController {

    private final BorrowBookUseCase borrowBook;

    LoanController(BorrowBookUseCase borrowBook) {
        this.borrowBook = borrowBook;
    }

    @PostMapping
    ResponseEntity<?> borrow(@RequestBody LoanRequest request) {
        BorrowBookResult result = borrowBook.borrow(request.toCommand());

        return switch (result) {
            case BorrowBookResult.Granted granted -> ResponseEntity
                    .created(URI.create("/loans/" + granted.loanId()))
                    .body(LoanResponse.from(granted));
            case BorrowBookResult.Refused(BorrowBookResult.Reason reason) -> ResponseEntity
                    .of(refusal(reason))
                    .build();
        };
    }

    private static ProblemDetail refusal(BorrowBookResult.Reason reason) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, switch (reason) {
            case BOOK_ALREADY_ON_LOAN -> "This book is already on loan.";
            case LOAN_QUOTA_REACHED -> "The member already holds as many books as their tier allows.";
            case OVERDUE_LOANS -> "The member must first return their overdue books.";
        });
        problem.setTitle("Loan refused");
        problem.setProperty("reason", reason.name());
        return problem;
    }
}
