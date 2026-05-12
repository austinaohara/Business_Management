package edu.farmingdale;

import edu.farmingdale.model.enums.ThemePreference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    @FXML private HBox navDashboard;
    @FXML private HBox navInventory;
    @FXML private HBox navSupplier;
    @FXML private HBox navCustomer;
    @FXML private HBox navSales;
    @FXML private Button themeToggleButton;
    @FXML private Button logoutButton;

    private final Map<String, Node> pageCache = new HashMap<>();
    private final Map<String, Refreshable> controllerCache = new HashMap<>();
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @FXML
    public void initialize() {
        applyTheme();
        loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml");
        navDashboard.setOnMouseClicked(e -> loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml"));
        navInventory.setOnMouseClicked(e -> loadPage(navInventory, "/edu/farmingdale/inventory.fxml"));
        navSupplier.setOnMouseClicked(e -> loadPage(navSupplier, "/edu/farmingdale/supplier.fxml"));
        navCustomer.setOnMouseClicked(e -> loadPage(navCustomer, "/edu/farmingdale/customer.fxml"));
        navSales.setOnMouseClicked(e -> loadPage(navSales, "/edu/farmingdale/sales.fxml"));
    }

    @FXML
    private void onToggleTheme() {
        themePreference = themePreference == ThemePreference.DARK
                ? ThemePreference.LIGHT
                : ThemePreference.DARK;
        applyTheme();
    }

    @FXML
    private void onLogout() {
        String currentUsername = UserSession.getInstance().getCurrentUser().getUsername();
        DatabaseManager.backupUserDatabase(currentUsername);

        UserSession.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/farmingdale/login.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styling/main.css").toExternalForm());

            LoginController loginController = loader.getController();
            if (loginController != null) {
                loginController.setThemePreference(themePreference);
            }

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(750);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference == null ? ThemePreference.LIGHT : themePreference;
        applyTheme();
    }

    private void applyTheme() {
        rootPane.getStyleClass().remove("dark-mode");
        if (themePreference == ThemePreference.DARK) {
            rootPane.getStyleClass().add("dark-mode");
        }
        themeToggleButton.setText(themePreference == ThemePreference.DARK ? "💡On" : "Lights Off");
        if (logoutButton != null && !logoutButton.getStyleClass().contains("sidebar-footer-button")) {
            logoutButton.getStyleClass().add("sidebar-footer-button");
        }
    }

    private void loadPage(HBox selectedNav, String path) {
        for (HBox nav : new HBox[]{navDashboard, navInventory, navSupplier, navCustomer, navSales}) {
            nav.getStyleClass().remove("nav-item-active");
            if (!nav.getStyleClass().contains("nav-item")) {
                nav.getStyleClass().add("nav-item");
            }
        }
        selectedNav.getStyleClass().remove("nav-item");
        selectedNav.getStyleClass().add("nav-item-active");

        if (!pageCache.containsKey(path)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Node page = loader.load();
                pageCache.put(path, page);
                Object controller = loader.getController();
                if (controller instanceof Refreshable) {
                    controllerCache.put(path, (Refreshable) controller);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            Refreshable controller = controllerCache.get(path);
            if (controller != null) {
                controller.refresh();
            }
        }

        contentArea.getChildren().setAll(pageCache.get(path));
    }
}
