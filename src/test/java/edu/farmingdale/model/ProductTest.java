package edu.farmingdale.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    @Test
    void isLowStockReturnsTrueWhenQuantityEqualsMinimumStock() {
        Product product = new Product(
                "Widget",
                "Hardware",
                "Stocked item",
                new BigDecimal("2.50"),
                new BigDecimal("5.00"),
                4,
                4,
                1,
                "A1"
        );

        assertTrue(product.isLowStock());
    }

    @Test
    void getInventoryValueUsesUnitCostAndQuantityOnHand() {
        Product product = new Product(
                "Widget",
                "Hardware",
                null,
                new BigDecimal("2.50"),
                new BigDecimal("5.00"),
                4,
                1,
                null,
                null
        );

        assertEquals(new BigDecimal("10.00"), product.getInventoryValue());
    }

    @Test
    void consumeStockRejectsAmountGreaterThanQuantityOnHand() {
        Product product = new Product(
                "Widget",
                "Hardware",
                null,
                new BigDecimal("2.50"),
                new BigDecimal("5.00"),
                4,
                1,
                null,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.consumeStock(5)
        );

        assertEquals("amount must not exceed quantityOnHand.", exception.getMessage());
    }

    @Test
    void constructorRejectsNegativeSellPrice() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "Widget",
                        "Hardware",
                        null,
                        new BigDecimal("2.50"),
                        new BigDecimal("-1.00"),
                        4,
                        1,
                        null,
                        null
                )
        );

        assertEquals("sellPrice must not be negative.", exception.getMessage());
    }
}
