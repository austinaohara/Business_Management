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

    private final Map<String, Node> pageCache = new HashMap<>();

    @FXML
    public void initialize() {
        loadPage("/edu/farmingdale/dashboard.fxml");
        navDashboard.setOnMouseClicked(e -> loadPage("/edu/farmingdale/dashboard.fxml"));
        navInventory.setOnMouseClicked(e -> loadPage("/edu/farmingdale/inventory.fxml"));
    }

    private void loadPage(String path) {
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
