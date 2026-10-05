package dev.hexagonal101.library.application.port.in;

/** The command refers to a book that is not in the catalog. */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String bookId) {
        super("Unknown book " + bookId);
    }
}
