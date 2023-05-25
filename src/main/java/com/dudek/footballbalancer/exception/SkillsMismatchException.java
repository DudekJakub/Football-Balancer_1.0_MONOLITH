package com.dudek.footballbalancer.exception;

public class SkillsMismatchException extends RuntimeException {
    public SkillsMismatchException(String reason) {
        super(reason);
    }

    public SkillsMismatchException() {
        super("Skills mismatch detected! Each player in specific room must have exactly the same skill types what room has!");
    }
}
