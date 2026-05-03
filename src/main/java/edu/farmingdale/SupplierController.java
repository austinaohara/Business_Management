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
import java.util.ArrayList;
import java.util.List;

public class SupplierController implements Refreshable {

    @FXML private VBox newOrderForm;
    @FXML private TextField supplierNameField, productNameField, quantityField, dueDateField, priorityField;
    @FXML private TextField searchField;
    @FXML private TextArea notesArea;
    @FXML private VBox deliveryRows;
    @FXML private VBox supplierRows;
    @FXML private HBox deliveryTemplateRow;
    @FXML private Label activeSuppliersLabel, activeOrdersLabel, nextDeliveryLabel, deliveredLabel;
    private final List<RowData> allSupplierRows = new ArrayList<>();

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(quantityField);
        TextFieldFormatter.applyIntegerFilter(priorityField);
        TextFieldFormatter.applyDateFormatter(dueDateField);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applySupplierFilter());
        }
        loadStats();
        loadDeliveries();
        loadSuppliers();
    }

    @Override
    public void refresh() {
        loadStats();
        loadDeliveries();
        loadSuppliers();
    }

    private void loadStats() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Suppliers")) {
                if (rs.next() && activeSuppliersLabel != null)
                    activeSuppliersLabel.setText(String.valueOf(rs.getInt(1)));
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM SupplierOrders WHERE status='Pending'")) {
                if (rs.next() && activeOrdersLabel != null)
                    activeOrdersLabel.setText(String.valueOf(rs.getInt(1)));
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT due_date FROM SupplierOrders WHERE status='Pending' " +
                     "AND due_date IS NOT NULL AND due_date <> '' " +
                     "ORDER BY order_id ASC FETCH FIRST 1 ROW ONLY")) {
                if (nextDeliveryLabel != null)
                    nextDeliveryLabel.setText(rs.next() ? rs.getString("due_date") : "—");
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM SupplierOrders WHERE status='Delivered'")) {
                if (rs.next() && deliveredLabel != null)
                    deliveredLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
        loadStats();
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
        loadStats();
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
