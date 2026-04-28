package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class SupplierController {

    @FXML private VBox newOrderForm;

    @FXML
    private void onNewOrderRequest() {
        newOrderForm.setVisible(true);
        newOrderForm.setManaged(true);
    }

    @FXML
    private void onCancelOrder() {
        newOrderForm.setVisible(false);
        newOrderForm.setManaged(false);
    }
}
