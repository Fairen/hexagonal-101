package dev.hexagonal101.library.adapter.out.s3;

import java.util.List;

import dev.hexagonal101.library.application.port.out.LoanRepository;
import dev.hexagonal101.library.domain.BookId;
import dev.hexagonal101.library.domain.Loan;
import dev.hexagonal101.library.domain.MemberId;

/** {@link LoanRepository} port, implemented on library.json in S3. */
class S3LoanRepository implements LoanRepository {

    private final S3LibraryDocument document;

    S3LoanRepository(S3LibraryDocument document) {
        this.document = document;
    }

    @Override
    public List<Loan> findActiveByMember(MemberId memberId) {
        return document.read().loans().stream()
                .filter(loan -> loan.memberId().equals(memberId.value()))
                .map(LibraryJson.LoanJson::toDomain)
                .toList();
    }

    @Override
    public boolean isBookOnLoan(BookId bookId) {
        return document.read().loans().stream().anyMatch(loan -> loan.bookId().equals(bookId.value()));
    }

    @Override
    public void save(Loan loan) {
        document.update(library -> library.withLoan(loan));
    }
}
