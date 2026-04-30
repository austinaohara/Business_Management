package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;

public class Product {
    private final Integer id;
    private String name;
    private String category;
    private String description;
    private BigDecimal unitCost;
    private BigDecimal sellPrice;
    private int quantityOnHand;
    private int minimumStock;
    private Integer supplierId;
    private String storageLocation;

    public Product(
            String name,
            String category,
            String description,
            BigDecimal unitCost,
            BigDecimal sellPrice,
            int quantityOnHand,
            int minimumStock,
            Integer supplierId,
            String storageLocation
    ) {
        this(
                null,
                name,
                category,
                description,
                unitCost,
                sellPrice,
                quantityOnHand,
                minimumStock,
                supplierId,
                storageLocation
        );
    }

    public Product(
            Integer id,
            String name,
            String category,
            String description,
            BigDecimal unitCost,
            BigDecimal sellPrice,
            int quantityOnHand,
            int minimumStock,
            Integer supplierId,
            String storageLocation
    ) {
        this.id = validateIdentifier(id, "id");
        this.name = ModelValidation.requireNonBlank(name, "name").trim();
        this.category = ModelValidation.requireNonBlank(category, "category").trim();
        this.description = normalizeOptionalText(description);
        this.unitCost = ModelValidation.requireNonNegative(unitCost, "unitCost");
        this.sellPrice = ModelValidation.requireNonNegative(sellPrice, "sellPrice");
        this.quantityOnHand = ModelValidation.requireNonNegative(quantityOnHand, "quantityOnHand");
        this.minimumStock = ModelValidation.requireNonNegative(minimumStock, "minimumStock");
        this.supplierId = validateIdentifier(supplierId, "supplierId");
        this.storageLocation = normalizeOptionalText(storageLocation);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void rename(String name) {
        this.name = ModelValidation.requireNonBlank(name, "name").trim();
    }

    public void updateCategory(String category) {
        this.category = ModelValidation.requireNonBlank(category, "category").trim();
    }

    public void updateDescription(String description) {
        this.description = normalizeOptionalText(description);
    }

    public void updatePricing(BigDecimal unitCost, BigDecimal sellPrice) {
        this.unitCost = ModelValidation.requireNonNegative(unitCost, "unitCost");
        this.sellPrice = ModelValidation.requireNonNegative(sellPrice, "sellPrice");
    }

    public void restock(int amount) {
        requirePositive(amount, "amount");
        quantityOnHand += amount;
    }

    public void consumeStock(int amount) {
        requirePositive(amount, "amount");
        if (amount > quantityOnHand) {
            throw new IllegalArgumentException("amount must not exceed quantityOnHand.");
        }
        quantityOnHand -= amount;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = ModelValidation.requireNonNegative(minimumStock, "minimumStock");
    }

    public void assignSupplier(Integer supplierId) {
        this.supplierId = validateIdentifier(supplierId, "supplierId");
    }

    public void moveTo(String storageLocation) {
        this.storageLocation = ModelValidation.requireNonBlank(storageLocation, "storageLocation").trim();
    }

    public boolean isLowStock() {
        return quantityOnHand <= minimumStock;
    }

    public BigDecimal getInventoryValue() {
        return unitCost.multiply(BigDecimal.valueOf(quantityOnHand));
    }

    private static Integer validateIdentifier(Integer id, String fieldName) {
        if (id == null) {
            return null;
        }
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
        return id;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}
