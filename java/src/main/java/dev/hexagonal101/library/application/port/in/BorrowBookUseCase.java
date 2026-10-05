package dev.hexagonal101.library.application.port.in;

/**
 * Driving port: "a member wants to borrow a book".
 * <p>
 * The REST controller and the Kafka listener both call this same interface.
 * Its input and output types are defined here, next to it: an adapter that calls the port
 * needs nothing else from the hexagon, and never sees the domain model.
 */
public interface BorrowBookUseCase {

    /**
     * @throws MemberNotFoundException if the member does not exist
     * @throws BookNotFoundException   if the book does not exist
     */
    BorrowBookResult borrow(BorrowBookCommand command);
}
