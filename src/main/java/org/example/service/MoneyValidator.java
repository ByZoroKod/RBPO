package org.example.service;

import org.example.exception.ValidationException;

import java.math.BigDecimal;

final class MoneyValidator {

    private static final int MAX_SCALE = 2;
    private static final int MAX_INTEGER_DIGITS = 17;

    private MoneyValidator() {
    }

    static void requirePositive(BigDecimal value, String notPositiveMessage) {
        if (value == null || value.signum() <= 0) {
            throw new ValidationException(notPositiveMessage);
        }
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() > MAX_SCALE) {
            throw new ValidationException("Допускается не более двух знаков после запятой");
        }
        if (normalized.precision() - normalized.scale() > MAX_INTEGER_DIGITS) {
            throw new ValidationException("Слишком большое значение суммы");
        }
    }
}
