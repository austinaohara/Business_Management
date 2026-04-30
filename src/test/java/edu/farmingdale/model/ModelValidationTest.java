package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelValidationTest {

    @Test
    void requireRangeSupportsComparableTypes() {
        BigDecimal rating = new BigDecimal("4.5");

        BigDecimal validatedRating = ModelValidation.requireRange(
                rating,
                BigDecimal.ZERO,
                BigDecimal.valueOf(5),
                "rating"
        );

        assertEquals(rating, validatedRating);
    }

    @Test
    void requireOrderRejectsReversedDates() {
        LocalDate start = LocalDate.of(2026, 5, 2);
        LocalDate end = LocalDate.of(2026, 5, 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ModelValidation.requireOrder(start, end, "startDate", "endDate")
        );

        assertEquals("endDate must not be before startDate.", exception.getMessage());
    }

    @Test
    void requireNonBlankRejectsBlankFieldLabel() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ModelValidation.requireNonBlank("value", " ")
        );

        assertEquals("fieldLabel must not be blank.", exception.getMessage());
    }
}
