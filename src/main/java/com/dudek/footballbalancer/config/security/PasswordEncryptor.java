package com.dudek.footballbalancer.config.security;

import java.util.Optional;

public interface PasswordEncryptor {

    Optional<String> encrypt(final String password);
    boolean checkPasswordMatch(final String savedPassword, final String providedPassword);
}
