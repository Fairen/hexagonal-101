package dev.hexagonal101.library.domain;

import java.time.Period;

/**
 * The membership tier decides how many books a member may hold at once,
 * and for how long.
 */
public enum MembershipTier {

    STANDARD(3, Period.ofDays(14)),
    PREMIUM(5, Period.ofDays(28));

    private final int maxActiveLoans;
    private final Period loanDuration;

    MembershipTier(int maxActiveLoans, Period loanDuration) {
        this.maxActiveLoans = maxActiveLoans;
        this.loanDuration = loanDuration;
    }

    public int maxActiveLoans() {
        return maxActiveLoans;
    }

    public Period loanDuration() {
        return loanDuration;
    }
}
