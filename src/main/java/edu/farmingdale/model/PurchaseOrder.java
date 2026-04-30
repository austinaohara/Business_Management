package edu.farmingdale.model;

import edu.farmingdale.model.enums.PurchaseOrderStatus;
import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PurchaseOrder {
    private static final int MIN_PRIORITY = 1;
    private static final int MAX_PRIORITY = 10;

    private final Integer id;
    private final Integer supplierId;
    private final LocalDate requestedDate;
    private LocalDate expectedDeliveryDate;
    private PurchaseOrderStatus status;
    private int priority;
    private BigDecimal budget;
    private String notes;
    private final List<PurchaseOrderItem> items;

    public PurchaseOrder(
            Integer supplierId,
            LocalDate requestedDate,
            LocalDate expectedDeliveryDate,
            PurchaseOrderStatus status,
            int priority,
            BigDecimal budget,
            String notes
    ) {
        this(null, supplierId, requestedDate, expectedDeliveryDate, status, priority, budget, notes, List.of());
    }

    public PurchaseOrder(
            Integer supplierId,
            LocalDate requestedDate,
            LocalDate expectedDeliveryDate,
            PurchaseOrderStatus status,
            int priority,
            BigDecimal budget,
            String notes,
            List<PurchaseOrderItem> items
    ) {
        this(null, supplierId, requestedDate, expectedDeliveryDate, status, priority, budget, notes, items);
    }

    public PurchaseOrder(
            Integer id,
            Integer supplierId,
            LocalDate requestedDate,
            LocalDate expectedDeliveryDate,
            PurchaseOrderStatus status,
            int priority,
            BigDecimal budget,
            String notes,
            List<PurchaseOrderItem> items
    ) {
        LocalDate validatedRequestedDate = ModelValidation.requireNotNull(requestedDate, "requestedDate");
        LocalDate validatedExpectedDeliveryDate = ModelValidation.requireNotNull(
                expectedDeliveryDate,
                "expectedDeliveryDate"
        );

        this.id = validateIdentifier(id, "id");
        this.supplierId = validateRequiredIdentifier(supplierId, "supplierId");
        ModelValidation.requireDateOrder(
                validatedRequestedDate,
                validatedExpectedDeliveryDate,
                "requestedDate",
                "expectedDeliveryDate"
        );
        this.requestedDate = validatedRequestedDate;
        this.expectedDeliveryDate = validatedExpectedDeliveryDate;
        this.status = ModelValidation.requireNotNull(status, "status");
        this.priority = validatePriority(priority);
        this.budget = validateBudget(budget);
        this.notes = normalizeOptionalText(notes);
        this.items = validateItems(items);
        validateFulfillmentRequirements(this.status, this.items);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public String getNotes() {
        return notes;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        ensureOpenForStructuralChanges();

        LocalDate validatedExpectedDeliveryDate = ModelValidation.requireNotNull(
                expectedDeliveryDate,
                "expectedDeliveryDate"
        );
        ModelValidation.requireDateOrder(
                requestedDate,
                validatedExpectedDeliveryDate,
                "requestedDate",
                "expectedDeliveryDate"
        );
        this.expectedDeliveryDate = validatedExpectedDeliveryDate;
    }

    public void changeStatus(PurchaseOrderStatus status) {
        PurchaseOrderStatus validatedStatus = ModelValidation.requireNotNull(status, "status");
        validateFulfillmentRequirements(validatedStatus, items);
        this.status = validatedStatus;
    }

    public void setPriority(int priority) {
        ensureOpenForStructuralChanges();
        this.priority = validatePriority(priority);
    }

    public void setBudget(BigDecimal budget) {
        ensureOpenForStructuralChanges();
        this.budget = validateBudget(budget);
    }

    public void setNotes(String notes) {
        this.notes = normalizeOptionalText(notes);
    }

    public void addItem(PurchaseOrderItem item) {
        ensureOpenForStructuralChanges();

        PurchaseOrderItem validatedItem = ModelValidation.requireNotNull(item, "item");
        ensureUniqueProduct(validatedItem.getProductId());
        items.add(validatedItem);
    }

    public void removeItem(Integer productId) {
        ensureOpenForStructuralChanges();

        Integer validatedProductId = validateRequiredIdentifier(productId, "productId");
        boolean removed = items.removeIf(item -> item.getProductId().equals(validatedProductId));
        if (!removed) {
            throw new IllegalArgumentException("productId was not found in order items.");
        }
    }

    public List<PurchaseOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getEstimatedTotal() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItem item : items) {
            totalAmount = totalAmount.add(item.getLineTotal());
        }
        return totalAmount;
    }

    private void ensureUniqueProduct(Integer productId) {
        for (PurchaseOrderItem existingItem : items) {
            if (existingItem.getProductId().equals(productId)) {
                throw new IllegalArgumentException("Duplicate productId is not allowed in order items.");
            }
        }
    }

    private void ensureOpenForStructuralChanges() {
        if (status == PurchaseOrderStatus.RECEIVED || status == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Closed purchase orders cannot be structurally modified.");
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

    private static int validatePriority(int priority) {
        return ModelValidation.requireRange(priority, MIN_PRIORITY, MAX_PRIORITY, "priority");
    }

    private static BigDecimal validateBudget(BigDecimal budget) {
        if (budget == null) {
            return null;
        }
        return ModelValidation.requireNonNegative(budget, "budget");
    }

    private static List<PurchaseOrderItem> validateItems(List<PurchaseOrderItem> items) {
        List<PurchaseOrderItem> validatedItems = new ArrayList<>();
        Set<Integer> productIds = new HashSet<>();

        for (PurchaseOrderItem item : ModelValidation.requireNotNull(items, "items")) {
            PurchaseOrderItem validatedItem = ModelValidation.requireNotNull(item, "item");
            if (!productIds.add(validatedItem.getProductId())) {
                throw new IllegalArgumentException("Duplicate productId is not allowed in order items.");
            }
            validatedItems.add(validatedItem);
        }

        return validatedItems;
    }

    private static void validateFulfillmentRequirements(
            PurchaseOrderStatus status,
            List<PurchaseOrderItem> items
    ) {
        if ((status == PurchaseOrderStatus.ORDERED
                || status == PurchaseOrderStatus.IN_TRANSIT
                || status == PurchaseOrderStatus.RECEIVED)
                && items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ordered, in-transit, or received purchase orders must contain at least one item."
            );
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
