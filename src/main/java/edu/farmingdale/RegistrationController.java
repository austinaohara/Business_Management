package edu.farmingdale;

import edu.farmingdale.model.enums.ThemePreference;
import edu.farmingdale.repository.StaffProfileDataRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

public class RegistrationController {

    private static final String COMMON_PASSWORD_WORD = "password";
    private static final String COMMON_NUMBER_SEQUENCE = "1234567";
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile(".*[^A-Za-z0-9\\s].*");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(".*\\s.*");

    @FXML private BorderPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private TextField visiblePasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Button passwordVisibilityButton;
    @FXML private Button confirmPasswordVisibilityButton;
    @FXML private Label statusLabel;

    private final StaffProfileDataRepository staffProfileRepository = new StaffProfileDataRepository();
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @FXML
    public void initialize() {
        clearStatus();
        initializePasswordStrength();
        initializePasswordFields();
        applyTheme();
    }

    @FXML
    private void onCreateAccount() {
        String username = getEnteredUsername();
        String password = getEnteredPassword();
        String confirm  = getEnteredConfirmationPassword();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showStatus("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            showStatus("Passwords do not match.");
            return;
        }

        if (staffProfileRepository.usernameExists(username)) {
            showStatus("Username already taken. Please choose another.");
            return;
        }

        if (!staffProfileRepository.registerUser(username, password)) {
            showStatus("Registration failed. Please try again.");
            return;
        }

        navigateToLogin();
    }

    @FXML
    private void onSignIn() {
        navigateToLogin();
    }

    @FXML
    private void onTogglePasswordVisibility() {
        setPasswordVisible(
                visiblePasswordField,
                passwordField,
                passwordVisibilityButton,
                !visiblePasswordField.isVisible()
        );
    }

    @FXML
    private void onToggleConfirmPasswordVisibility() {
        setPasswordVisible(
                visibleConfirmPasswordField,
                confirmPasswordField,
                confirmPasswordVisibilityButton,
                !visibleConfirmPasswordField.isVisible()
        );
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference == null ? ThemePreference.LIGHT : themePreference;
        if (rootPane != null) {
            applyTheme();
        }
    }

    private void navigateToLogin() {
        try {
            var url = getClass().getResource("/edu/farmingdale/login.fxml");
            if (url == null) {
                showStatus("Login page resource not found.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styling/main.css").toExternalForm());

            LoginController loginController = loader.getController();
            if (loginController != null) {
                loginController.setThemePreference(themePreference);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(750);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyTheme() {
        rootPane.getStyleClass().remove("dark-mode");
        if (themePreference == ThemePreference.DARK) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private void initializePasswordStrength() {
        refreshPasswordStrength();
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> refreshPasswordStrength());
    }

    private void refreshPasswordStrength() {
        int score = calculatePasswordStrength(passwordField.getText());
        passwordStrengthLabel.setText("Password Strength " + score + "/10");
        updatePasswordStrengthStyle(score);
    }

    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        if (containsCommonWeakPattern(password)) {
            return 3;
        }

        int score = 0;

        if (password.length() >= 8) {
            score += 2;
        }
        if (password.length() >= 12) {
            score += 2;
        }
        if (LOWERCASE_PATTERN.matcher(password).matches()) {
            score += 1;
        }
        if (UPPERCASE_PATTERN.matcher(password).matches()) {
            score += 1;
        }
        if (DIGIT_PATTERN.matcher(password).matches()) {
            score += 1;
        }
        if (SPECIAL_CHARACTER_PATTERN.matcher(password).matches()) {
            score += 2;
        }
        if (!WHITESPACE_PATTERN.matcher(password).matches()) {
            score += 1;
        }

        return Math.min(score, 10);
    }

    private boolean containsCommonWeakPattern(String password) {
        String normalizedPassword = password.toLowerCase();
        return normalizedPassword.contains(COMMON_PASSWORD_WORD)
                && normalizedPassword.contains(COMMON_NUMBER_SEQUENCE);
    }

    private void updatePasswordStrengthStyle(int score) {
        passwordStrengthLabel.getStyleClass().removeAll(
                "password-strength-weak",
                "password-strength-medium",
                "password-strength-strong"
        );

        if (score <= 3) {
            passwordStrengthLabel.getStyleClass().add("password-strength-weak");
            return;
        }

        if (score <= 7) {
            passwordStrengthLabel.getStyleClass().add("password-strength-medium");
            return;
        }

        passwordStrengthLabel.getStyleClass().add("password-strength-strong");
    }

    private void initializePasswordFields() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        setPasswordVisible(visiblePasswordField, passwordField, passwordVisibilityButton, false);
        setPasswordVisible(visibleConfirmPasswordField, confirmPasswordField, confirmPasswordVisibilityButton, false);
    }

    private String getEnteredUsername() {
        return usernameField.getText() == null ? "" : usernameField.getText().trim();
    }

    private String getEnteredPassword() {
        return getFieldText(visiblePasswordField.isVisible() ? visiblePasswordField : passwordField);
    }

    private String getEnteredConfirmationPassword() {
        return getFieldText(visibleConfirmPasswordField.isVisible() ? visibleConfirmPasswordField : confirmPasswordField);
    }

    private String getFieldText(TextInputControl field) {
        return field.getText() == null ? "" : field.getText();
    }

    private void setPasswordVisible(
            TextField visibleField,
            PasswordField hiddenField,
            Button toggleButton,
            boolean visible
    ) {
        visibleField.setManaged(visible);
        visibleField.setVisible(visible);
        hiddenField.setManaged(!visible);
        hiddenField.setVisible(!visible);
        toggleButton.setText(visible ? "Hide" : "Show");

        if (visible) {
            visibleField.requestFocus();
            visibleField.positionCaret(visibleField.getText().length());
            return;
        }

        hiddenField.requestFocus();
        hiddenField.positionCaret(hiddenField.getText().length());
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
