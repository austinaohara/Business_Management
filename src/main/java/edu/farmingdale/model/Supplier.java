package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;

import java.math.BigDecimal;

public class Supplier {
    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAX_RATING = BigDecimal.valueOf(5);

    private final Integer id;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private Integer leadTimeDays;
    private String paymentInfo;
    private BigDecimal rating;

    public Supplier(
            String name,
            String contactName,
            String email,
            String phone,
            Integer leadTimeDays,
            String paymentInfo,
            BigDecimal rating
    ) {
        this(null, name, contactName, email, phone, leadTimeDays, paymentInfo, rating);
    }

    public Supplier(
            Integer id,
            String name,
            String contactName,
            String email,
            String phone,
            Integer leadTimeDays,
            String paymentInfo,
            BigDecimal rating
    ) {
        this.id = validateIdentifier(id, "id");
        this.name = ModelValidation.requireNonBlank(name, "name").trim();
        this.contactName = normalizeOptionalText(contactName);
        this.email = normalizeOptionalText(email);
        this.phone = normalizeOptionalText(phone);
        this.leadTimeDays = validateNullableNonNegative(leadTimeDays, "leadTimeDays");
        this.paymentInfo = normalizeOptionalText(paymentInfo);
        this.rating = validateRating(rating);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void rename(String name) {
        this.name = ModelValidation.requireNonBlank(name, "name").trim();
    }

    public void updateContact(String contactName, String email, String phone) {
        this.contactName = normalizeOptionalText(contactName);
        this.email = normalizeOptionalText(email);
        this.phone = normalizeOptionalText(phone);
    }

    public void updateLeadTime(Integer leadTimeDays) {
        this.leadTimeDays = validateNullableNonNegative(leadTimeDays, "leadTimeDays");
    }

    public void updatePaymentInfo(String paymentInfo) {
        this.paymentInfo = normalizeOptionalText(paymentInfo);
    }

    public void updateRating(BigDecimal rating) {
        this.rating = validateRating(rating);
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

    private static Integer validateNullableNonNegative(Integer value, String fieldName) {
        if (value == null) {
            return null;
        }
        return ModelValidation.requireNonNegative(value, fieldName);
    }

    private static BigDecimal validateRating(BigDecimal rating) {
        if (rating == null) {
            return null;
        }

        ModelValidation.requireNonNegative(rating, "rating");
        if (rating.compareTo(MAX_RATING) > 0 || rating.compareTo(MIN_RATING) < 0) {
            throw new IllegalArgumentException("rating must be between 0 and 5.");
        }
        return rating;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
