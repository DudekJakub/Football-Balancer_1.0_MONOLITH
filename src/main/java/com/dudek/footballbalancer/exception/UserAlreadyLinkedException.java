package com.dudek.footballbalancer.exception;

public class UserAlreadyLinkedException extends RuntimeException {
    public UserAlreadyLinkedException(String reason) {
        super(reason);
    }

    public UserAlreadyLinkedException() {
        super("User already linked! Specific user can be linked only with one player per room!");
    }
}
