package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class InventoryController {

    @FXML private VBox addProductForm;
    @FXML private TextField nameField, categoryField, stockField, minStockField, priceField, supplierField;
    @FXML private VBox productRows;
    @FXML private HBox templateRow;
    @FXML private Label totalProductsLabel, totalValueLabel, lowStockLabel;

    @FXML
    public void initialize() { loadProducts(); }

    @FXML
    private void onAddProduct() {
        addProductForm.setVisible(true);
        addProductForm.setManaged(true);
    }

    @FXML
    private void onCancelProduct() {
        addProductForm.setVisible(false);
        addProductForm.setManaged(false);
        nameField.clear(); categoryField.clear(); stockField.clear();
        minStockField.clear(); priceField.clear(); supplierField.clear();
    }

    @FXML
    private void onSaveProduct() {
        if (nameField.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,sell_price,supplier) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, categoryField.getText().trim());
            ps.setInt(3, parseIntSafe(stockField.getText()));
            ps.setInt(4, parseIntSafe(minStockField.getText()));
            ps.setDouble(5, parseDoubleSafe(priceField.getText()));
            ps.setString(6, supplierField.getText().trim());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        onCancelProduct();
        loadProducts();
    }

    private void loadProducts() {
        productRows.getChildren().clear();
        int count = 0;
        double totalValue = 0;
        int lowStock = 0;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT id,name,category,quantity_on_hand,minimum_stock,sell_price,supplier FROM Inventory")) {
            while (rs.next()) {
                count++;
                int qty = rs.getInt("quantity_on_hand");
                int min = rs.getInt("minimum_stock");
                double price = rs.getDouble("sell_price");
                totalValue += qty * price;
                if (qty < min) lowStock++;
                HBox row = buildRow(
                    "PRD-" + String.format("%03d", rs.getInt("id")),
                    rs.getString("name"),
                    rs.getString("category"),
                    String.valueOf(qty),
                    String.format("$%.2f", price),
                    rs.getString("supplier") != null ? rs.getString("supplier") : ""
                );
                productRows.getChildren().add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (totalProductsLabel != null) totalProductsLabel.setText(String.valueOf(count));
        if (totalValueLabel != null) totalValueLabel.setText(String.format("$%,.0f", totalValue));
        if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(lowStock));
    }

    private HBox buildRow(String id, String name, String category, String stock, String price, String supplier) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        double[] widths = {110, 200, 130, 100, 100, 150, 80};
        String[] values = {id, name, category, stock, price, supplier, ""};
        String[] styles = {"table-cell", "table-cell", "category-badge", "table-cell", "table-cell", "table-cell", "table-cell"};
        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add(styles[i]);
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }
}
