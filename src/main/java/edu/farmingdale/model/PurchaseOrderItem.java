package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;

public class PurchaseOrderItem {
    private final Integer id;
    private final Integer productId;
    private int quantity;
    private BigDecimal unitCost;

    public PurchaseOrderItem(
            Integer productId,
            int quantity,
            BigDecimal unitCost
    ) {
        this(null, productId, quantity, unitCost);
    }

    public PurchaseOrderItem(
            Integer id,
            Integer productId,
            int quantity,
            BigDecimal unitCost
    ) {
        this.id = validateIdentifier(id, "id");
        this.productId = validateRequiredIdentifier(productId, "productId");
        this.quantity = ModelValidation.requireRange(quantity, 1, Integer.MAX_VALUE, "quantity");
        this.unitCost = ModelValidation.requireNonNegative(unitCost, "unitCost");
    }

    public Integer getId() {
        return id;
    }

    public Integer getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void changeQuantity(int quantity) {
        this.quantity = ModelValidation.requireRange(quantity, 1, Integer.MAX_VALUE, "quantity");
    }

    public void changeUnitCost(BigDecimal unitCost) {
        this.unitCost = ModelValidation.requireNonNegative(unitCost, "unitCost");
    }

    public BigDecimal getLineTotal() {
        return unitCost.multiply(BigDecimal.valueOf(quantity));
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
}
