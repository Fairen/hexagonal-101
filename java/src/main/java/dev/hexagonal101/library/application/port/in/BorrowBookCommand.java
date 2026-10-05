package dev.hexagonal101.library.application.port.in;

/**
 * The input of {@link BorrowBookUseCase}: an intent, independent of HTTP or Kafka.
 * <p>
 * It carries plain values, not domain objects: callers of the port only depend on the port.
 * Turning these values into domain objects is the job of the use case.
 */
public record BorrowBookCommand(String memberId, String bookId) {

    public BorrowBookCommand {
        if (memberId == null || memberId.isBlank() || bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("memberId and bookId are required");
        }
    }
}
