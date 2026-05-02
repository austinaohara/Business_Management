package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class SupplierController {

    @FXML private VBox newOrderForm;
    @FXML private TextField supplierNameField, productNameField, quantityField, dueDateField, priorityField;
    @FXML private TextArea notesArea;
    @FXML private VBox deliveryRows;
    @FXML private HBox deliveryTemplateRow;

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(quantityField);
        TextFieldFormatter.applyIntegerFilter(priorityField);
TextFieldFormatter.applyDateFormatter(dueDateField);
        loadDeliveries();
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
        if (supplierNameField.getText().trim().isEmpty() || productNameField.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,notes) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, supplierNameField.getText().trim());
            ps.setString(2, productNameField.getText().trim());
            ps.setInt(3, parseIntSafe(quantityField.getText()));
            ps.setString(4, dueDateField.getText().trim());
            ps.setInt(5, parseIntSafe(priorityField.getText()));
            ps.setString(6, notesArea.getText().trim());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        onCancelOrder();
        loadDeliveries();
    }

    // when confirmed, adds qty to existing inventory item or creates a new one
    private void confirmDelivery(int orderId, String productName, String supplierName, int quantity) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                "SELECT id, quantity_on_hand FROM Inventory WHERE UPPER(name) = UPPER(?)");
            check.setString(1, productName);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int existingId  = rs.getInt("id");
                int newQty = rs.getInt("quantity_on_hand") + quantity;
                PreparedStatement update = conn.prepareStatement(
                    "UPDATE Inventory SET quantity_on_hand=? WHERE id=?");
                update.setInt(1, newQty);
                update.setInt(2, existingId);
                update.executeUpdate();
            } else {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,sell_price,supplier) VALUES(?,?,?,?,?,?)");
                insert.setString(1, productName);
                insert.setString(2, "");
                insert.setInt(3, quantity);
                insert.setDouble(4, 0);
                insert.setDouble(5, 0);
                insert.setString(6, supplierName);
                insert.executeUpdate();
            }

            PreparedStatement markDone = conn.prepareStatement(
                "UPDATE SupplierOrders SET status='Delivered' WHERE order_id=?");
            markDone.setInt(1, orderId);
            markDone.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }
        loadDeliveries();
    }

    private void loadDeliveries() {
        deliveryRows.getChildren().clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT order_id,supplier_name,product_name,quantity,due_date,priority,notes,status FROM SupplierOrders ORDER BY due_date")) {
            while (rs.next()) {
                int orderId      = rs.getInt("order_id");
                String supplier  = rs.getString("supplier_name");
                String product   = rs.getString("product_name");
                int qty          = rs.getInt("quantity");
                String status    = rs.getString("status");
                String statusText = status != null ? status : "Pending";

                HBox row = new HBox();
                row.getStyleClass().add("table-row");
                row.setAlignment(Pos.CENTER_LEFT);

                row.getChildren().add(cell(supplier, 120));
                row.getChildren().add(cell(product, 120));
                row.getChildren().add(cell(String.valueOf(qty), 50));
                row.getChildren().add(cell(rs.getString("due_date") != null ? rs.getString("due_date") : "", 95));
                row.getChildren().add(cell(String.valueOf(rs.getInt("priority")), 55));

                String notes = rs.getString("notes");
                row.getChildren().add(cell(notes != null && !notes.isEmpty() ? notes : "—", 120));

                Label statusLabel = new Label(statusText);
                statusLabel.setPrefWidth(90);
                statusLabel.getStyleClass().add("Delivered".equals(statusText) ? "status-in-transit" : "status-pending");
                row.getChildren().add(statusLabel);

                // confirm button — replaced with a label once delivered
                if ("Delivered".equals(statusText)) {
                    Label done = new Label("Delivered");
                    done.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 0 12;");
                    row.getChildren().add(done);
                } else {
                    Button confirmBtn = new Button("Confirm");
                    confirmBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-translate-x: 10;");
                    String finalSupplier = supplier;
                    String finalProduct  = product;
                    confirmBtn.setOnAction(e -> confirmDelivery(orderId, finalProduct, finalSupplier, qty));
                    row.getChildren().add(confirmBtn);
                }

                deliveryRows.getChildren().add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
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

}
