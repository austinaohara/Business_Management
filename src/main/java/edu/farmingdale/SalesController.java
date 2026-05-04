package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesController implements Refreshable {

    @FXML private VBox recordSaleForm;
    @FXML private ComboBox<String> saleProductCombo;
    @FXML private TextField saleQuantityField, salePriceField;
    @FXML private Label availableLabel;
    @FXML private TextField searchField;
    @FXML private VBox saleRows;
    @FXML private HBox templateRow;
    @FXML private Label totalRevenueLabel, totalSalesLabel, itemsSoldLabel;

    private final List<RowData> allRows = new ArrayList<>();
    private final Map<String, Double> productPrices = new HashMap<>();
    private final Map<String, Integer> productQuantities = new HashMap<>();

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(saleQuantityField);
        TextFieldFormatter.applyDecimalFilter(salePriceField);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        }
        loadSales();
    }

    @Override
    public void refresh() {
        loadSales();
    }

    @FXML
    private void onRecordSale() {
        loadProductsIntoCombo();
        recordSaleForm.setVisible(true);
        recordSaleForm.setManaged(true);
    }

    @FXML
    private void onCancelSale() {
        recordSaleForm.setVisible(false);
        recordSaleForm.setManaged(false);
        saleProductCombo.getSelectionModel().clearSelection();
        saleProductCombo.setValue(null);
        saleQuantityField.clear();
        salePriceField.clear();
        availableLabel.setText("");
    }

    private void loadProductsIntoCombo() {
        productPrices.clear();
        productQuantities.clear();
        saleProductCombo.getItems().clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT name, sell_price, quantity_on_hand FROM Inventory " +
                 "WHERE quantity_on_hand > 0 ORDER BY name")) {
            while (rs.next()) {
                String name = rs.getString("name");
                productPrices.put(name, rs.getDouble("sell_price"));
                productQuantities.put(name, rs.getInt("quantity_on_hand"));
                saleProductCombo.getItems().add(name);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        saleProductCombo.setOnAction(e -> {
            String selected = saleProductCombo.getValue();
            if (selected != null) {
                double price = productPrices.getOrDefault(selected, 0.0);
                int available = productQuantities.getOrDefault(selected, 0);
                if (price > 0) {
                    salePriceField.setText(String.format("%.2f", price));
                } else {
                    salePriceField.clear();
                }
                availableLabel.setText("In stock: " + available + " units");
                availableLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 12px;");
            }
        });
    }

    @FXML
    private void onSaveSale() {
        String product = saleProductCombo.getValue();
        int qty = parseIntSafe(saleQuantityField.getText());
        double unitPrice = parseDoubleSafe(salePriceField.getText());
        if (product == null || product.isEmpty() || qty <= 0 || salePriceField.getText().trim().isEmpty()) return;
        int available = productQuantities.getOrDefault(product, 0);
        if (qty > available) return;
        double total = qty * unitPrice;

        try (Connection conn = DatabaseManager.getConnection()) {

            PreparedStatement orderPs = conn.prepareStatement(
                "INSERT INTO Orders(customer_id, amount, status, order_date) VALUES(NULL,?,?,CURRENT_DATE)",
                Statement.RETURN_GENERATED_KEYS);
            orderPs.setDouble(1, total);
            orderPs.setString(2, "Completed");
            orderPs.executeUpdate();
            ResultSet keys = orderPs.getGeneratedKeys();
            if (!keys.next()) return;
            int orderId = keys.getInt(1);

            PreparedStatement itemPs = conn.prepareStatement(
                "INSERT INTO OrderItems(order_id, product_name, quantity, unit_price) VALUES(?,?,?,?)");
            itemPs.setInt(1, orderId);
            itemPs.setString(2, product);
            itemPs.setInt(3, qty);
            itemPs.setDouble(4, unitPrice);
            itemPs.executeUpdate();

            PreparedStatement inv = conn.prepareStatement(
                "UPDATE Inventory SET quantity_on_hand = quantity_on_hand - ? WHERE UPPER(name) = UPPER(?)");
            inv.setInt(1, qty);
            inv.setString(2, product);
            inv.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }

        onCancelSale();
        loadSales();
    }

    private void loadSales() {
        allRows.clear();
        int totalSales = 0;
        int totalItems = 0;
        double totalRevenue = 0;

        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT o.order_id, oi.product_name, oi.quantity AS qty, oi.unit_price, " +
                 "o.amount, o.order_date, o.status " +
                 "FROM Orders o JOIN OrderItems oi ON o.order_id = oi.order_id " +
                 "ORDER BY o.order_date DESC, o.order_id DESC")) {
            while (rs.next()) {
                totalSales++;
                totalItems += rs.getInt("qty");
                totalRevenue += rs.getDouble("amount");
                String saleId = "ORD-" + String.format("%03d", rs.getInt("order_id"));
                String product = rs.getString("product_name");
                String qty = String.valueOf(rs.getInt("qty"));
                String up = NumberFormat.getCurrencyInstance(Locale.US).format(rs.getDouble("unit_price"));
                String amount = NumberFormat.getCurrencyInstance(Locale.US).format(rs.getDouble("amount"));
                String date = rs.getString("order_date") != null ? rs.getString("order_date") : "";
                String status = rs.getString("status") != null ? rs.getString("status") : "Completed";
                HBox row = buildRow(saleId, product, qty, up, amount, date, status);
                allRows.add(new RowData(
                    String.join(" ", saleId, product, date, status).toLowerCase(), row));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        if (totalRevenueLabel != null) totalRevenueLabel.setText(currency.format(totalRevenue));
        if (totalSalesLabel != null)   totalSalesLabel.setText(String.valueOf(totalSales));
        if (itemsSoldLabel != null)    itemsSoldLabel.setText(String.valueOf(totalItems));
        applyFilter();
    }

    private HBox buildRow(String saleId, String product, String qty,
                          String unitPrice, String total, String date, String status) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        double[] widths = {110, 200, 70, 110, 110, 110};
        String[] values = {saleId, product, qty, unitPrice, total, date};
        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add("table-cell");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("status-completed");
        row.getChildren().add(statusLabel);
        return row;
    }

    private void applyFilter() {
        saleRows.getChildren().clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (RowData rd : allRows) {
            if (query.isEmpty() || rd.searchableText.contains(query)) {
                saleRows.getChildren().add(rd.row);
            }
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    private static class RowData {
        private final String searchableText;
        private final HBox row;
        RowData(String searchableText, HBox row) {
            this.searchableText = searchableText;
            this.row = row;
        }
    }
}
