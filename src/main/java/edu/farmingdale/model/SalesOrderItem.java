package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;

public class SalesOrderItem {
    private final Integer id;
    private final Integer productId;
    private int quantity;
    private BigDecimal unitPrice;

    public SalesOrderItem(
            Integer productId,
            int quantity,
            BigDecimal unitPrice
    ) {
        this(null, productId, quantity, unitPrice);
    }

    public SalesOrderItem(
            Integer id,
            Integer productId,
            int quantity,
            BigDecimal unitPrice
    ) {
        this.id = validateIdentifier(id, "id");
        this.productId = validateRequiredIdentifier(productId, "productId");
        this.quantity = ModelValidation.requireRange(quantity, 1, Integer.MAX_VALUE, "quantity");
        this.unitPrice = ModelValidation.requireNonNegative(unitPrice, "unitPrice");
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void changeQuantity(int quantity) {
        this.quantity = ModelValidation.requireRange(quantity, 1, Integer.MAX_VALUE, "quantity");
    }

    public void changeUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = ModelValidation.requireNonNegative(unitPrice, "unitPrice");
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
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
