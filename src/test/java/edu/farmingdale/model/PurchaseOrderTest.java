package edu.farmingdale.model;

import edu.farmingdale.model.enums.PurchaseOrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseOrderTest {

    @Test
    void constructorRejectsExpectedDeliveryBeforeRequestedDate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseOrder(
                        5,
                        LocalDate.of(2026, 5, 2),
                        LocalDate.of(2026, 5, 1),
                        PurchaseOrderStatus.DRAFT,
                        5,
                        null
                )
        );

        assertEquals("expectedDeliveryDate must not be before requestedDate.", exception.getMessage());
    }

    @Test
    void constructorRejectsOrderedStatusWithoutItems() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PurchaseOrder(
                        5,
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 3),
                        PurchaseOrderStatus.ORDERED,
                        5,
                        null,
                        List.of()
                )
        );

        assertEquals(
                "Ordered, in-transit, or received purchase orders must contain at least one item.",
                exception.getMessage()
        );
    }

    @Test
    void estimatedTotalIsDerivedFromLineItems() {
        PurchaseOrder order = new PurchaseOrder(
                5,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                PurchaseOrderStatus.ORDERED,
                5,
                null,
                List.of(
                        new PurchaseOrderItem(1, 2, new BigDecimal("7.50")),
                        new PurchaseOrderItem(2, 1, new BigDecimal("12.00"))
                )
        );

        assertEquals(new BigDecimal("27.00"), order.getEstimatedTotal());
    }

    @Test
    void receivedOrderCannotBeStructurallyModified() {
        PurchaseOrder order = new PurchaseOrder(
                5,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                PurchaseOrderStatus.ORDERED,
                5,
                null,
                List.of(new PurchaseOrderItem(1, 2, new BigDecimal("7.50")))
        );
        order.changeStatus(PurchaseOrderStatus.RECEIVED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.addItem(new PurchaseOrderItem(2, 1, new BigDecimal("12.00")))
        );

        assertEquals("Closed purchase orders cannot be structurally modified.", exception.getMessage());
    }
}
