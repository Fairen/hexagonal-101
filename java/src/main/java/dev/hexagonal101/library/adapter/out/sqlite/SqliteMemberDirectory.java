package dev.hexagonal101.library.adapter.out.sqlite;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

import dev.hexagonal101.library.application.port.out.MemberDirectory;
import dev.hexagonal101.library.domain.Member;
import dev.hexagonal101.library.domain.MemberId;
import dev.hexagonal101.library.domain.MembershipTier;

/** {@link MemberDirectory} port, implemented with SQL on the {@code member} table. */
class SqliteMemberDirectory implements MemberDirectory {

    private final JdbcClient jdbc;

    SqliteMemberDirectory(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return jdbc.sql("SELECT id, name, tier FROM member WHERE id = :id")
                .param("id", id.value())
                .query((row, rowNum) -> new Member(
                        new MemberId(row.getString("id")), row.getString("name"),
                        MembershipTier.valueOf(row.getString("tier"))))
                .optional();
    }
}
