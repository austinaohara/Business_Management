package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class InventoryController {

    @FXML private VBox addProductForm;

    @FXML
    private void onAddProduct() {
        addProductForm.setVisible(true);
        addProductForm.setManaged(true);
    }

    @FXML
    private void onCancelProduct() {
        addProductForm.setVisible(false);
        addProductForm.setManaged(false);
    }
}
