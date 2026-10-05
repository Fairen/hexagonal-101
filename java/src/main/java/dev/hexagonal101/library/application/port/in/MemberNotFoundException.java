package dev.hexagonal101.library.application.port.in;

/** The command refers to a member the library does not know. */
public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String memberId) {
        super("Unknown member " + memberId);
    }
}
