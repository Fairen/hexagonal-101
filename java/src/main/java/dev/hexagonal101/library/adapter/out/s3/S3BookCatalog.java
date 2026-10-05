package dev.hexagonal101.library.adapter.out.s3;

import java.util.Optional;

import dev.hexagonal101.library.application.port.out.BookCatalog;
import dev.hexagonal101.library.domain.Book;
import dev.hexagonal101.library.domain.BookId;

/** {@link BookCatalog} port, implemented by reading library.json from S3. */
class S3BookCatalog implements BookCatalog {

    private final S3LibraryDocument document;

    S3BookCatalog(S3LibraryDocument document) {
        this.document = document;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return document.read().books().stream()
                .filter(book -> book.id().equals(id.value()))
                .findFirst()
                .map(LibraryJson.BookJson::toDomain);
    }
}
