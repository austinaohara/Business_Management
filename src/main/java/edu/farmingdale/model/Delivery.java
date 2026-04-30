package edu.farmingdale.model;

import edu.farmingdale.model.enums.DeliveryStatus;
import edu.farmingdale.model.validation.ModelValidation;

import java.time.LocalDate;

public class Delivery {
    private final Integer id;
    private final Integer purchaseOrderId;
    private LocalDate deliveryDate;
    private DeliveryStatus status;
    private String notes;

    public Delivery(
            Integer purchaseOrderId,
            LocalDate deliveryDate,
            DeliveryStatus status,
            String notes
    ) {
        this(null, purchaseOrderId, deliveryDate, status, notes);
    }

    public Delivery(
            Integer id,
            Integer purchaseOrderId,
            LocalDate deliveryDate,
            DeliveryStatus status,
            String notes
    ) {
        this.id = validateIdentifier(id, "id");
        this.purchaseOrderId = validateRequiredIdentifier(purchaseOrderId, "purchaseOrderId");
        this.deliveryDate = ModelValidation.requireNotNull(deliveryDate, "deliveryDate");
        this.status = ModelValidation.requireNotNull(status, "status");
        this.notes = normalizeOptionalText(notes);
    }

    public Integer getId() {
        return id;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public void reschedule(LocalDate deliveryDate) {
        this.deliveryDate = ModelValidation.requireNotNull(deliveryDate, "deliveryDate");
    }

    public void changeStatus(DeliveryStatus status) {
        this.status = ModelValidation.requireNotNull(status, "status");
    }

    public void setNotes(String notes) {
        this.notes = normalizeOptionalText(notes);
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

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
