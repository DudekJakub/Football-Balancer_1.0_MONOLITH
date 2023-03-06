package com.dudek.footballbalancer.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Slf4j
@Component
public class PasswordEncryptorSHA256 implements PasswordEncryptor {

    @Override
    public Optional<String> encrypt(final String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Optional.of(toHexString(hash));
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            log.error("Error occurred with algorithm SHA-256! " + noSuchAlgorithmException.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean checkPasswordMatch(final String savedPassword, final String providedPassword) {
        return savedPassword.equals(encrypt(providedPassword).orElse(null));
    }

    private String toHexString(final byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
