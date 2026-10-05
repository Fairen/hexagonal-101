package dev.hexagonal101.library.adapter.in.kafka;

import org.jspecify.annotations.Nullable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

import dev.hexagonal101.library.application.port.in.BookNotFoundException;
import dev.hexagonal101.library.application.port.in.BorrowBookUseCase;
import dev.hexagonal101.library.application.port.in.MemberNotFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consumes loan requests, calls {@link BorrowBookUseCase}, publishes the outcome.
 * <p>
 * Kafka is asynchronous: there is no caller waiting for a return value. The answer is sent
 * to a reply topic ({@link SendTo}). Answering is a transport concern, so it stays in the adapter:
 * the use case is exactly the one the REST controller calls.
 */
class LoanRequestListener {

    static final String REQUEST_TOPIC = "library.loan-requests";
    static final String RESULT_TOPIC = "library.loan-results";

    private final BorrowBookUseCase borrowBook;
    private final JsonMapper json;

    LoanRequestListener(BorrowBookUseCase borrowBook, JsonMapper json) {
        this.borrowBook = borrowBook;
        this.json = json;
    }

    @KafkaListener(topics = REQUEST_TOPIC, groupId = "library")
    @SendTo(RESULT_TOPIC)
    String onLoanRequest(String payload) {
        return json.writeValueAsString(handle(payload));
    }

    private LoanResultMessage handle(String payload) {
        LoanRequestMessage request;
        try {
            request = json.readValue(payload, LoanRequestMessage.class);
        } catch (JacksonException e) {
            return LoanResultMessage.failed(null, LoanResultMessage.Status.INVALID, "Malformed JSON");
        }

        @Nullable String requestId = request.requestId();
        try {
            return LoanResultMessage.of(requestId, borrowBook.borrow(request.toCommand()));
        } catch (MemberNotFoundException | BookNotFoundException e) {
            return LoanResultMessage.failed(requestId, LoanResultMessage.Status.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return LoanResultMessage.failed(requestId, LoanResultMessage.Status.INVALID, e.getMessage());
        }
    }
}
