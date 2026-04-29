package edu.farmingdale.model.validation;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ModelValidation {

    private ModelValidation() {
    }

    public static String requireNonBlank(String value, String fieldName) {
        requireNonBlank(fieldName, "fieldName");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldName) {
        requireNonBlank(fieldName, "fieldName");
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        requireNotNull(value, fieldName);
        requireNonBlank(fieldName, "fieldName");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative.");
        }
        return value;
    }

    public static int requireRange(int value, int min, int max, String fieldName) {
        requireNonBlank(fieldName, "fieldName");
        if (min > max) {
            throw new IllegalArgumentException("min must not be greater than max.");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + "."
            );
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String fieldName) {
        requireNonBlank(fieldName, "fieldName");
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
        return value;
    }

    public static void requireDateOrder(
            LocalDate start,
            LocalDate end,
            String startField,
            String endField
    ) {
        requireNotNull(start, startField);
        requireNotNull(end, endField);
        requireNonBlank(startField, "startField");
        requireNonBlank(endField, "endField");

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    endField + " must not be before " + startField + "."
            );
        }
    }
}
