package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class SupplierController {

    @FXML private VBox newOrderForm;
    @FXML private TextField supplierNameField, productNameField, quantityField, dueDateField, priorityField, budgetField;
    @FXML private TextArea notesArea;
    @FXML private VBox deliveryRows;
    @FXML private HBox deliveryTemplateRow;

    @FXML
    public void initialize() { loadDeliveries(); }

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
        dueDateField.clear(); priorityField.clear(); budgetField.clear(); notesArea.clear();
    }

    @FXML
    private void onSubmitOrder() {
        if (supplierNameField.getText().trim().isEmpty() || productNameField.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,budget,notes) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, supplierNameField.getText().trim());
            ps.setString(2, productNameField.getText().trim());
            ps.setInt(3, parseIntSafe(quantityField.getText()));
            ps.setString(4, dueDateField.getText().trim());
            ps.setInt(5, parseIntSafe(priorityField.getText()));
            ps.setDouble(6, parseDoubleSafe(budgetField.getText()));
            ps.setString(7, notesArea.getText().trim());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        onCancelOrder();
        loadDeliveries();
    }

    private void loadDeliveries() {
        deliveryRows.getChildren().clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT supplier_name,due_date,product_name,quantity,status FROM SupplierOrders ORDER BY due_date")) {
            while (rs.next()) {
                HBox row = new HBox();
                row.getStyleClass().add("table-row");
                row.setAlignment(Pos.CENTER_LEFT);

                Label supplier = new Label(rs.getString("supplier_name"));
                supplier.getStyleClass().add("table-cell");
                supplier.setPrefWidth(300);

                String dateStr = rs.getString("due_date");
                Label date = new Label(dateStr != null ? dateStr : "");
                date.getStyleClass().add("table-cell");
                date.setPrefWidth(200);

                int qty = rs.getInt("quantity");
                Label items = new Label(rs.getString("product_name") + " (" + qty + ")");
                items.getStyleClass().add("table-cell");
                items.setPrefWidth(300);

                String status = rs.getString("status");
                Label statusLabel = new Label(status != null ? status : "Pending");
                statusLabel.getStyleClass().add(
                    "Pending".equals(status) ? "status-pending" : "status-in-transit");

                row.getChildren().addAll(supplier, date, items, statusLabel);
                deliveryRows.getChildren().add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }
}
