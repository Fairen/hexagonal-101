package dev.hexagonal101.library.application.port.out;

import java.util.Optional;

import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;

/** Driven port: where members are looked up. Implemented by the S3 or the SQLite adapter. */
public interface MemberDirectory {

    Optional<Member> findById(MemberId id);
}
