package edu.farmingdale;

import edu.farmingdale.model.enums.ThemePreference;
import edu.farmingdale.repository.StaffProfileDataRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RegistrationController {

    @FXML private BorderPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label statusLabel;

    private final StaffProfileDataRepository staffProfileRepository = new StaffProfileDataRepository();
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @FXML
    public void initialize() {
        clearStatus();
        initializePasswordStrength();
        applyTheme();
    }

    @FXML
    private void onCreateAccount() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirm  = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

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
        passwordStrengthLabel.setText("Password Strength 0/10");
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
