package org.example.service;

import org.example.exception.ValidationException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void validate(String password, String username) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new ValidationException("Пароль должен содержать не менее " + MIN_LENGTH + " символов");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new ValidationException("Пароль слишком длинный: не более " + MAX_BYTES + " байт");
        }
        if (password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new ValidationException("Пароль должен содержать заглавную и строчную буквы и цифру");
        }
        if (username != null && password.toLowerCase().contains(username.toLowerCase())) {
            throw new ValidationException("Пароль не должен содержать логин");
        }
    }
}
