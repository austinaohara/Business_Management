package edu.farmingdale.model;

import edu.farmingdale.model.validation.ModelValidation;

public class Customer {
    private final Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public Customer(
            String firstName,
            String lastName,
            String email,
            String phone
    ) {
        this(null, firstName, lastName, email, phone);
    }

    public Customer(
            Integer id,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {
        this.id = validateIdentifier(id, "id");
        this.firstName = ModelValidation.requireNonBlank(firstName, "firstName").trim();
        this.lastName = ModelValidation.requireNonBlank(lastName, "lastName").trim();
        this.email = normalizeOptionalText(email);
        this.phone = normalizeOptionalText(phone);
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void updateName(String firstName, String lastName) {
        this.firstName = ModelValidation.requireNonBlank(firstName, "firstName").trim();
        this.lastName = ModelValidation.requireNonBlank(lastName, "lastName").trim();
    }

    public void updateContact(String email, String phone) {
        this.email = normalizeOptionalText(email);
        this.phone = normalizeOptionalText(phone);
    }

    public String getFullName() {
        return firstName + " " + lastName;
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
}
