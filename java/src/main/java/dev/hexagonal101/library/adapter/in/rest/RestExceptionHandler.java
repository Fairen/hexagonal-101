package dev.hexagonal101.library.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.hexagonal101.library.application.port.in.BookNotFoundException;
import dev.hexagonal101.library.application.port.in.MemberNotFoundException;

/** Translates the exceptions of the use case into HTTP statuses (RFC 9457 problem details). */
@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler({MemberNotFoundException.class, BookNotFoundException.class})
    ProblemDetail notFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
