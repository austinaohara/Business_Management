package edu.farmingdale.model;

import edu.farmingdale.model.enums.ThemePreference;
import edu.farmingdale.model.validation.ModelValidation;

public class StaffProfile {
    private final Integer id;
    private String username;
    private String passwordHash;
    private ThemePreference themePreference;

    public StaffProfile(
            String username,
            String passwordHash,
            ThemePreference themePreference
    ) {
        this(null, username, passwordHash, themePreference);
    }

    public StaffProfile(
            Integer id,
            String username,
            String passwordHash,
            ThemePreference themePreference
    ) {
        this.id = validateIdentifier(id, "id");
        this.username = ModelValidation.requireNonBlank(username, "username").trim();
        this.passwordHash = ModelValidation.requireNonBlank(passwordHash, "passwordHash").trim();
        this.themePreference = ModelValidation.requireNotNull(themePreference, "themePreference");
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public ThemePreference getThemePreference() {
        return themePreference;
    }

    public void changeUsername(String username) {
        this.username = ModelValidation.requireNonBlank(username, "username").trim();
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = ModelValidation.requireNonBlank(passwordHash, "passwordHash").trim();
    }

    public void changeTheme(ThemePreference themePreference) {
        this.themePreference = ModelValidation.requireNotNull(themePreference, "themePreference");
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
}
