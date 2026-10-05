package dev.hexagonal101.library.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.hexagonal101.library.application.port.in.BorrowBookResult;
import dev.hexagonal101.library.application.port.in.BorrowBookUseCase;
import dev.hexagonal101.library.application.port.in.MemberNotFoundException;

/** The adapter is tested against a mock of the port: only the HTTP translation is under test. */
@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private BorrowBookUseCase borrowBook;

    @Test
    void returns_201_with_the_loan_when_granted() {
        given(borrowBook.borrow(any())).willReturn(new BorrowBookResult.Granted(
                "l-1", "m-1", "b-1", LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 16)));

        assertThat(post("{\"memberId\":\"m-1\",\"bookId\":\"b-1\"}"))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/loans/l-1")
                .bodyJson().extractingPath("$.dueOn").isEqualTo("2026-10-16");
    }

    @Test
    void returns_409_with_the_reason_when_refused() {
        given(borrowBook.borrow(any())).willReturn(new BorrowBookResult.Refused(BorrowBookResult.Reason.LOAN_QUOTA_REACHED));

        assertThat(post("{\"memberId\":\"m-2\",\"bookId\":\"b-1\"}"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.reason").isEqualTo("LOAN_QUOTA_REACHED");
    }

    @Test
    void returns_404_for_an_unknown_member() {
        given(borrowBook.borrow(any())).willThrow(new MemberNotFoundException("nobody"));

        assertThat(post("{\"memberId\":\"nobody\",\"bookId\":\"b-1\"}")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void returns_400_when_a_field_is_missing() {
        assertThat(post("{\"memberId\":\"m-1\"}")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    private MockMvcTester.MockMvcRequestBuilder post(String body) {
        return mvc.post().uri("/loans").contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
