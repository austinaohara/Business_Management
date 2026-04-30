package edu.farmingdale.model;

import edu.farmingdale.model.enums.SalesOrderStatus;
import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesOrder {
    private final Integer id;
    private final Integer customerId;
    private final LocalDate orderDate;
    private SalesOrderStatus status;
    private final List<SalesOrderItem> items;

    public SalesOrder(
            Integer customerId,
            LocalDate orderDate,
            SalesOrderStatus status
    ) {
        this(null, customerId, orderDate, status, List.of());
    }

    public SalesOrder(
            Integer customerId,
            LocalDate orderDate,
            SalesOrderStatus status,
            List<SalesOrderItem> items
    ) {
        this(null, customerId, orderDate, status, items);
    }

    public SalesOrder(
            Integer id,
            Integer customerId,
            LocalDate orderDate,
            SalesOrderStatus status,
            List<SalesOrderItem> items
    ) {
        this.id = validateIdentifier(id, "id");
        this.customerId = validateRequiredIdentifier(customerId, "customerId");
        this.orderDate = ModelValidation.requireNotNull(orderDate, "orderDate");
        this.status = ModelValidation.requireNotNull(status, "status");
        this.items = validateItems(items);
        validateCompletionRequirements(this.status, this.items);
    }

    public Integer getId() {
        return id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public void addItem(SalesOrderItem item) {
        ensureNotCompleted();

        SalesOrderItem validatedItem = ModelValidation.requireNotNull(item, "item");
        ensureUniqueProduct(validatedItem.getProductId());
        items.add(validatedItem);
    }

    public void removeItem(Integer productId) {
        ensureNotCompleted();

        Integer validatedProductId = validateRequiredIdentifier(productId, "productId");
        boolean removed = items.removeIf(item -> item.getProductId().equals(validatedProductId));
        if (!removed) {
            throw new IllegalArgumentException("productId was not found in order items.");
        }
    }

    public void changeStatus(SalesOrderStatus status) {
        SalesOrderStatus validatedStatus = ModelValidation.requireNotNull(status, "status");
        validateCompletionRequirements(validatedStatus, items);
        this.status = validatedStatus;
    }

    public List<SalesOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderItem item : items) {
            totalAmount = totalAmount.add(item.getLineTotal());
        }
        return totalAmount;
    }

    public int getTotalUnits() {
        int totalUnits = 0;
        for (SalesOrderItem item : items) {
            totalUnits += item.getQuantity();
        }
        return totalUnits;
    }

    private void ensureUniqueProduct(Integer productId) {
        for (SalesOrderItem existingItem : items) {
            if (existingItem.getProductId().equals(productId)) {
                throw new IllegalArgumentException("Duplicate productId is not allowed in order items.");
            }
        }
    }

    private void ensureNotCompleted() {
        if (status == SalesOrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed sales orders cannot be modified.");
        }
    }

    private static Integer validateIdentifier(Integer id, String fieldLabel) {
        if (id == null) {
            return null;
        }
        if (id <= 0) {
            throw new IllegalArgumentException(fieldLabel + " must be positive.");
        }
        return id;
    }

    private static Integer validateRequiredIdentifier(Integer id, String fieldLabel) {
        ModelValidation.requireNotNull(id, fieldLabel);
        if (id <= 0) {
            throw new IllegalArgumentException(fieldLabel + " must be positive.");
        }
        return id;
    }

    private static List<SalesOrderItem> validateItems(List<SalesOrderItem> items) {
        List<SalesOrderItem> validatedItems = new ArrayList<>();
        Set<Integer> productIds = new HashSet<>();

        for (SalesOrderItem item : ModelValidation.requireNotNull(items, "items")) {
            SalesOrderItem validatedItem = ModelValidation.requireNotNull(item, "item");
            if (!productIds.add(validatedItem.getProductId())) {
                throw new IllegalArgumentException("Duplicate productId is not allowed in order items.");
            }
            validatedItems.add(validatedItem);
        }

        return validatedItems;
    }

    private static void validateCompletionRequirements(SalesOrderStatus status, List<SalesOrderItem> items) {
        if (status == SalesOrderStatus.COMPLETED && items.isEmpty()) {
            throw new IllegalArgumentException("Completed sales orders must contain at least one item.");
        }
    }
}
