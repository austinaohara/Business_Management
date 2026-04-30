package edu.farmingdale.model.validation;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ModelValidation {
    private static final String FIELD_LABEL_NAME = "fieldLabel";

    private ModelValidation() {
    }

    public static String requireNonBlank(String value, String fieldLabel) {
        String normalizedFieldLabel = requireFieldLabel(fieldLabel);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(normalizedFieldLabel + " must not be blank.");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldLabel) {
        String normalizedFieldLabel = requireFieldLabel(fieldLabel);
        if (value < 0) {
            throw new IllegalArgumentException(normalizedFieldLabel + " must not be negative.");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String fieldLabel) {
        String normalizedFieldLabel = requireFieldLabel(fieldLabel);
        requireNotNull(value, normalizedFieldLabel);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(normalizedFieldLabel + " must not be negative.");
        }
        return value;
    }

    public static <T extends Comparable<? super T>> T requireRange(
            T value,
            T min,
            T max,
            String fieldLabel
    ) {
        String normalizedFieldLabel = requireFieldLabel(fieldLabel);
        requireNotNull(value, normalizedFieldLabel);
        requireNotNull(min, "min");
        requireNotNull(max, "max");

        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min must not be greater than max.");
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                    normalizedFieldLabel + " must be between " + min + " and " + max + "."
            );
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String fieldLabel) {
        String normalizedFieldLabel = requireFieldLabel(fieldLabel);
        if (value == null) {
            throw new IllegalArgumentException(normalizedFieldLabel + " must not be null.");
        }
        return value;
    }

    public static <T extends Comparable<? super T>> void requireOrder(
            T start,
            T end,
            String startFieldLabel,
            String endFieldLabel
    ) {
        String normalizedStartFieldLabel = requireFieldLabel(startFieldLabel);
        String normalizedEndFieldLabel = requireFieldLabel(endFieldLabel);
        requireNotNull(start, normalizedStartFieldLabel);
        requireNotNull(end, normalizedEndFieldLabel);

        if (end.compareTo(start) < 0) {
            throw new IllegalArgumentException(
                    normalizedEndFieldLabel + " must not be before " + normalizedStartFieldLabel + "."
            );
        }
    }

    public static void requireDateOrder(
            LocalDate start,
            LocalDate end,
            String startFieldLabel,
            String endFieldLabel
    ) {
        requireOrder(start, end, startFieldLabel, endFieldLabel);
    }

    private static String requireFieldLabel(String fieldLabel) {
        if (fieldLabel == null || fieldLabel.isBlank()) {
            throw new IllegalArgumentException(FIELD_LABEL_NAME + " must not be blank.");
        }
        return fieldLabel.trim();
    }
}
