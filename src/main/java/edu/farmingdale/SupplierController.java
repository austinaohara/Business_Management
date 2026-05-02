package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierController {

    @FXML private VBox newOrderForm;
    @FXML private TextField supplierNameField, productNameField, quantityField, dueDateField, priorityField, budgetField;
    @FXML private TextField searchField;
    @FXML private TextArea notesArea;
    @FXML private VBox deliveryRows;
    @FXML private VBox supplierRows;
    @FXML private HBox deliveryTemplateRow;
    private final List<RowData> allSupplierRows = new ArrayList<>();

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applySupplierFilter());
        }
        loadDeliveries();
        loadSuppliers();
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

    private void loadSuppliers() {
        allSupplierRows.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id,name,contact_info,lead_time,payment_info FROM Suppliers ORDER BY name")) {
            while (rs.next()) {
                String supplierId = "SUP-" + String.format("%03d", rs.getInt("id"));
                String name = safeText(rs.getString("name"));
                String contactInfo = safeText(rs.getString("contact_info"));
                String leadTime = rs.getInt("lead_time") > 0 ? rs.getInt("lead_time") + " days" : "N/A";
                String paymentInfo = safeText(rs.getString("payment_info"));
                HBox row = buildSupplierRow(supplierId, name, contactInfo, leadTime, paymentInfo);
                String searchable = String.join(" ",
                        supplierId, name, contactInfo, leadTime, paymentInfo).toLowerCase();
                allSupplierRows.add(new RowData(searchable, row));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        applySupplierFilter();
    }

    private HBox buildSupplierRow(
            String supplierId,
            String name,
            String contactInfo,
            String leadTime,
            String paymentInfo
    ) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label(supplierId);
        idLabel.getStyleClass().add("table-cell");
        idLabel.setPrefWidth(110);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("table-cell");
        nameLabel.setPrefWidth(180);

        Label contactLabel = new Label(contactInfo);
        contactLabel.getStyleClass().add("table-cell");
        contactLabel.setPrefWidth(210);

        Label leadTimeLabel = new Label(leadTime);
        leadTimeLabel.getStyleClass().add("table-cell");
        leadTimeLabel.setPrefWidth(130);

        Label paymentLabel = new Label(paymentInfo);
        paymentLabel.getStyleClass().add("table-cell");
        paymentLabel.setPrefWidth(130);

        Label productsLabel = new Label("");
        productsLabel.getStyleClass().add("table-cell");
        productsLabel.setPrefWidth(180);

        Label actionsLabel = new Label("");
        actionsLabel.getStyleClass().add("table-cell");
        actionsLabel.setPrefWidth(80);

        row.getChildren().addAll(
                idLabel,
                nameLabel,
                contactLabel,
                leadTimeLabel,
                paymentLabel,
                productsLabel,
                actionsLabel
        );
        return row;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    private void applySupplierFilter() {
        if (supplierRows == null) {
            return;
        }
        supplierRows.getChildren().clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (RowData rowData : allSupplierRows) {
            if (query.isEmpty() || rowData.searchableText.contains(query)) {
                supplierRows.getChildren().add(rowData.row);
            }
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private static class RowData {
        private final String searchableText;
        private final HBox row;

        private RowData(String searchableText, HBox row) {
            this.searchableText = searchableText;
            this.row = row;
        }
    }
}
