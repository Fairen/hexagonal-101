package dev.hexagonal101.library.adapter.out.s3;

import java.util.Optional;

import dev.hexagonal101.library.application.port.out.MemberDirectory;
import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;

/** {@link MemberDirectory} port, implemented by reading library.json from S3. */
class S3MemberDirectory implements MemberDirectory {

    private final S3LibraryDocument document;

    S3MemberDirectory(S3LibraryDocument document) {
        this.document = document;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return document.read().members().stream()
                .filter(member -> member.id().equals(id.value()))
                .findFirst()
                .map(LibraryJson.MemberJson::toDomain);
    }
}
