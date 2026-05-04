package edu.farmingdale;

import edu.farmingdale.model.enums.ThemePreference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML private BorderPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private ThemePreference themePreference = ThemePreference.LIGHT;

    @FXML
    public void initialize() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        applyTheme();
    }

    @FXML
    private void onSignIn() {
        try {
            var url = getClass().getResource("/edu/farmingdale/login.fxml");
            if (url == null) {
                statusLabel.setText("Login page resource not found.");
                statusLabel.setVisible(true);
                statusLabel.setManaged(true);
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

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference == null ? ThemePreference.LIGHT : themePreference;
        if (rootPane != null) {
            applyTheme();
        }
    }

    private void applyTheme() {
        rootPane.getStyleClass().remove("dark-mode");
        if (themePreference == ThemePreference.DARK) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }
}
