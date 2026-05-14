package edu.farmingdale;

import edu.farmingdale.model.User;
import edu.farmingdale.model.enums.ThemePreference;
import edu.farmingdale.repository.StaffProfileDataRepository;
import edu.farmingdale.util.AuditLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private BorderPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ToggleButton themeToggleSwitch;
    @FXML private Label themeModeLabel;
    private final StaffProfileDataRepository staffProfileRepository = new StaffProfileDataRepository();
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @FXML
    public void initialize() {
        clearStatus();
        applyTheme();
    }

    @FXML
    private void onSignIn() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter a username and password.");
            return;
        }

        if (!staffProfileRepository.isValidCredentials(username, password)) {
            showStatus("Invalid username or password.");
            return;
        }

        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);
        AuditLogger.logAction(username, "LOGIN", "Successful authentication");
        openMainApp();
    }

    @FXML
    private void onToggleTheme() {
        themePreference = themeToggleSwitch.isSelected()
                ? ThemePreference.DARK
                : ThemePreference.LIGHT;
        applyTheme();
    }

    @FXML
    private void onSignUp() {
        try {
            var url = getClass().getResource("/edu/farmingdale/registration.fxml");
            if (url == null) {
                showStatus("Registration page resource not found.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styling/main.css").toExternalForm());

            RegistrationController registrationController = loader.getController();
            if (registrationController != null) {
                registrationController.setThemePreference(themePreference);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(750);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            showStatus("Unable to open the registration page.");
            e.printStackTrace();
        }
    }

    private void openMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/farmingdale/main_frame.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styling/main.css").toExternalForm());

            MainController mainController = loader.getController();
            if (mainController != null) {
                mainController.setThemePreference(themePreference);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(750);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showStatus("Unable to open the main application.");
            e.printStackTrace();
        }
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference == null ? ThemePreference.LIGHT : themePreference;
        applyTheme();
    }

    private void applyTheme() {
        rootPane.getStyleClass().remove("dark-mode");
        boolean darkModeEnabled = themePreference == ThemePreference.DARK;
        if (darkModeEnabled) {
            rootPane.getStyleClass().add("dark-mode");
        }
        themeToggleSwitch.setSelected(darkModeEnabled);
        themeModeLabel.setText(darkModeEnabled ? "Dark" : "Light");
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
