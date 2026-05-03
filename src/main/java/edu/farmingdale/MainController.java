package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private HBox navDashboard;
    @FXML private HBox navInventory;
    @FXML private HBox navSupplier;
    @FXML private HBox navCustomer;
    @FXML private HBox navSales;

    private final Map<String, Node> pageCache = new HashMap<>();
    private final Map<String, Refreshable> controllerCache = new HashMap<>();

    @FXML
    public void initialize() {
        loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml");
        navDashboard.setOnMouseClicked(e -> loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml"));
        navInventory.setOnMouseClicked(e -> loadPage(navInventory, "/edu/farmingdale/inventory.fxml"));
        navSupplier.setOnMouseClicked(e -> loadPage(navSupplier, "/edu/farmingdale/supplier.fxml"));
        navCustomer.setOnMouseClicked(e -> loadPage(navCustomer, "/edu/farmingdale/customer.fxml"));
        navSales.setOnMouseClicked(e -> loadPage(navSales, "/edu/farmingdale/sales.fxml"));
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
