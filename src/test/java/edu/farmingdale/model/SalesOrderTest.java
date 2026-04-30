package edu.farmingdale.model;

import edu.farmingdale.model.enums.SalesOrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesOrderTest {

    @Test
    void totalsAreDerivedFromLineItems() {
        SalesOrder order = new SalesOrder(
                10,
                LocalDate.of(2026, 4, 29),
                SalesOrderStatus.PENDING,
                List.of(
                        new SalesOrderItem(1, 2, new BigDecimal("10.00")),
                        new SalesOrderItem(2, 3, new BigDecimal("4.50"))
                )
        );

        assertEquals(new BigDecimal("33.50"), order.getTotalAmount());
        assertEquals(5, order.getTotalUnits());
    }

    @Test
    void constructorRejectsCompletedOrderWithoutItems() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SalesOrder(
                        10,
                        LocalDate.of(2026, 4, 29),
                        SalesOrderStatus.COMPLETED,
                        List.of()
                )
        );

        assertEquals("Completed sales orders must contain at least one item.", exception.getMessage());
    }

    @Test
    void addItemRejectsDuplicateProductIds() {
        SalesOrder order = new SalesOrder(
                10,
                LocalDate.of(2026, 4, 29),
                SalesOrderStatus.PENDING,
                List.of(new SalesOrderItem(1, 2, new BigDecimal("10.00")))
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.addItem(new SalesOrderItem(1, 1, new BigDecimal("9.00")))
        );

        assertEquals("Duplicate productId is not allowed in order items.", exception.getMessage());
    }

    @Test
    void completedOrderCannotBeModified() {
        SalesOrder order = new SalesOrder(
                10,
                LocalDate.of(2026, 4, 29),
                SalesOrderStatus.PENDING,
                List.of(new SalesOrderItem(1, 2, new BigDecimal("10.00")))
        );
        order.changeStatus(SalesOrderStatus.COMPLETED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.removeItem(1)
        );

        assertEquals("Completed sales orders cannot be modified.", exception.getMessage());
    }
}
