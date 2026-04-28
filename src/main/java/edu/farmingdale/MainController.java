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

    private final Map<String, Node> pageCache = new HashMap<>();

    @FXML
    public void initialize() {
        loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml");
        navDashboard.setOnMouseClicked(e -> loadPage(navDashboard, "/edu/farmingdale/dashboard.fxml"));
        navInventory.setOnMouseClicked(e -> loadPage(navInventory, "/edu/farmingdale/inventory.fxml"));
        navSupplier.setOnMouseClicked(e -> loadPage(navSupplier, "/edu/farmingdale/supplier.fxml"));
    }

    private void loadPage(HBox selectedNav, String path) {
        for (HBox nav : new HBox[]{navDashboard, navInventory, navSupplier}) {
            nav.getStyleClass().remove("nav-item-active");
            if (!nav.getStyleClass().contains("nav-item")) {
                nav.getStyleClass().add("nav-item");
            }
        }
        selectedNav.getStyleClass().remove("nav-item");
        selectedNav.getStyleClass().add("nav-item-active");

        //cache pages instead of having to load everything on app startup -- lazy load
        pageCache.computeIfAbsent(path, p -> {
            try {
                return FXMLLoader.load(getClass().getResource(p));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        contentArea.getChildren().setAll((Node) pageCache.get(path));
    }
}
