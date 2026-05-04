package edu.farmingdale;

import edu.farmingdale.repository.SupplierDataRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SupplierController implements Refreshable {

    @FXML private VBox newOrderForm;
    @FXML private TextField supplierNameField, productNameField, quantityField, dueDateField, priorityField;
    @FXML private TextArea notesArea;
    @FXML private VBox deliveryRows;
    @FXML private HBox deliveryTemplateRow;
    @FXML private Label activeSuppliersLabel, activeOrdersLabel, nextDeliveryLabel, deliveredLabel;

    private final SupplierDataRepository supplierRepository = new SupplierDataRepository();

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(quantityField);
        TextFieldFormatter.applyIntegerFilter(priorityField);
        TextFieldFormatter.applyDateFormatter(dueDateField);
        refresh();
    }

    @Override
    public void refresh() {
        SupplierDataRepository.SupplierViewData supplierData = supplierRepository.loadSupplierData();
        loadStats(supplierData.stats());
        loadDeliveries(supplierData.deliveries());
    }

    private void loadStats(SupplierDataRepository.SupplierStats stats) {
        if (activeSuppliersLabel != null) {
            activeSuppliersLabel.setText(String.valueOf(stats.activeSuppliers()));
        }
        if (activeOrdersLabel != null) {
            activeOrdersLabel.setText(String.valueOf(stats.activeOrders()));
        }
        if (nextDeliveryLabel != null) {
            nextDeliveryLabel.setText(
                    stats.nextDelivery() == null || stats.nextDelivery().isBlank() ? "-" : stats.nextDelivery()
            );
        }
        if (deliveredLabel != null) {
            deliveredLabel.setText(String.valueOf(stats.deliveredCount()));
        }
    }

    @FXML
    private void onNewOrderRequest() {
        newOrderForm.setVisible(true);
        newOrderForm.setManaged(true);
    }

    @FXML
    private void onCancelOrder() {
        newOrderForm.setVisible(false);
        newOrderForm.setManaged(false);
        supplierNameField.clear(); productNameField.clear(); quantityField.clear();
        dueDateField.clear(); priorityField.clear(); notesArea.clear();
    }

    @FXML
    private void onSubmitOrder() {
        if (supplierNameField.getText().trim().isEmpty() || productNameField.getText().trim().isEmpty()) {
            showWarning("Please provide both supplier name and product name.");
            return;
        }
        supplierRepository.createOrder(new SupplierDataRepository.SupplierOrderInput(
                supplierNameField.getText().trim(),
                productNameField.getText().trim(),
                parseIntSafe(quantityField.getText()),
                dueDateField.getText().trim(),
                parseIntSafe(priorityField.getText()),
                notesArea.getText().trim()
        ));
        onCancelOrder();
        refresh();
    }

    private void confirmDelivery(int orderId, String productName, String supplierName, int quantity) {
        supplierRepository.confirmDelivery(orderId, productName, supplierName, quantity);
        refresh();
    }

    private void loadDeliveries(Iterable<SupplierDataRepository.SupplierDeliveryRow> deliveries) {
        deliveryRows.getChildren().clear();
        for (SupplierDataRepository.SupplierDeliveryRow delivery : deliveries) {
            int orderId = delivery.orderId();
            String supplier = delivery.supplierName();
            String product = delivery.productName();
            int qty = delivery.quantity();
            String statusText = delivery.status();

            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setAlignment(Pos.CENTER_LEFT);

            row.getChildren().add(cell(supplier, 120));
            row.getChildren().add(cell(product, 120));
            row.getChildren().add(cell(String.valueOf(qty), 50));
            row.getChildren().add(cell(delivery.dueDate() != null ? delivery.dueDate() : "", 95));
            row.getChildren().add(cell(String.valueOf(delivery.priority()), 55));

            String notes = delivery.notes();
            row.getChildren().add(cell(notes != null && !notes.isEmpty() ? notes : "-", 120));

            Label statusLabel = new Label(statusText);
            statusLabel.setPrefWidth(90);
            statusLabel.getStyleClass().add("Delivered".equals(statusText) ? "status-in-transit" : "status-pending");
            row.getChildren().add(statusLabel);

            if ("Delivered".equals(statusText)) {
                Label done = new Label("Delivered");
                done.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 0 12;");
                row.getChildren().add(done);
            } else {
                Button confirmBtn = new Button("Confirm");
                confirmBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-translate-x: 10;");
                String finalSupplier = supplier;
                String finalProduct = product;
                confirmBtn.setOnAction(e -> confirmDelivery(orderId, finalProduct, finalSupplier, qty));
                row.getChildren().add(confirmBtn);
            }

            deliveryRows.getChildren().add(row);
        }
    }

    private Label cell(String text, double width) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("table-cell");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Missing Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
