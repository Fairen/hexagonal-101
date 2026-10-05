package dev.hexagonal101.library.domain;

import java.util.Objects;

/** A registered member of the library. */
public record Member(MemberId id, String name, MembershipTier tier) {

    public Member {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(tier, "tier");
    }
}
