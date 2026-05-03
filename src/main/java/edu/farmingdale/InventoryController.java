package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    @FXML private VBox addProductForm;
    @FXML private Label formTitleLabel;
    @FXML private TextField nameField, categoryField, stockField, unitPriceField, priceField, supplierField;
    @FXML private TextField searchField;
    @FXML private VBox productRows;
    @FXML private HBox templateRow;
    @FXML private Label totalProductsLabel, totalValueLabel, lowStockLabel;
    private final List<RowData> allRows = new ArrayList<>();

    private int editingId = -1;

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(stockField);
        TextFieldFormatter.applyDecimalFilter(unitPriceField);
        TextFieldFormatter.applyDecimalFilter(priceField);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        }
        loadProducts();
    }

    @FXML
    private void onAddProduct() {
        editingId = -1;
        formTitleLabel.setText("Add New Product");
        addProductForm.setVisible(true);
        addProductForm.setManaged(true);
    }

    @FXML
    private void onCancelProduct() {
        editingId = -1;
        formTitleLabel.setText("Add New Product");
        addProductForm.setVisible(false);
        addProductForm.setManaged(false);
        nameField.clear(); categoryField.clear(); stockField.clear();
        unitPriceField.clear(); priceField.clear(); supplierField.clear();
    }

    @FXML
    private void onSaveProduct() {
        if (nameField.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseManager.getConnection()) {
            if (editingId >= 0) {
                // updating an existing product
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Inventory SET name=?,category=?,quantity_on_hand=?,minimum_stock=?,sell_price=?,supplier=? WHERE id=?");
                ps.setString(1, nameField.getText().trim());
                ps.setString(2, categoryField.getText().trim());
                ps.setInt(3, parseIntSafe(stockField.getText()));
                ps.setDouble(4, parseDoubleSafe(unitPriceField.getText()));
                ps.setDouble(5, parseDoubleSafe(priceField.getText()));
                ps.setString(6, supplierField.getText().trim());
                ps.setInt(7, editingId);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,sell_price,supplier) VALUES(?,?,?,?,?,?)");
                ps.setString(1, nameField.getText().trim());
                ps.setString(2, categoryField.getText().trim());
                ps.setInt(3, parseIntSafe(stockField.getText()));
                ps.setDouble(4, parseDoubleSafe(unitPriceField.getText()));
                ps.setDouble(5, parseDoubleSafe(priceField.getText()));
                ps.setString(6, supplierField.getText().trim());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        onCancelProduct();
        loadProducts();
    }

    private void loadProducts() {
        allRows.clear();
        int count = 0;
        double totalValue = 0;
        int lowStock = 0;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT id,name,category,quantity_on_hand,minimum_stock,sell_price,supplier FROM Inventory")) {
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                int qty = rs.getInt("quantity_on_hand");
                int min = rs.getInt("minimum_stock");
                double unitPrice = rs.getDouble("minimum_stock");
                double price = rs.getDouble("sell_price");
                String name = rs.getString("name");
                String category = rs.getString("category");
                String supplier = rs.getString("supplier") != null ? rs.getString("supplier") : "";
                totalValue += qty * price;
                if (qty < min) lowStock++;
                HBox row = buildRow(
                    id, "PRD-" + String.format("%03d", id),
                    name, category,
                    String.valueOf(qty),
                    String.format("%.2f", unitPrice),
                    String.format("%.2f", price),
                    supplier
                );
                String searchable = String.join(" ",
                        idFromDb(rs.getInt("id")),
                        safeText(rs.getString("name")),
                        safeText(rs.getString("category")),
                        safeText(rs.getString("supplier"))
                ).toLowerCase();
                allRows.add(new RowData(searchable, row));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (totalProductsLabel != null) totalProductsLabel.setText(String.valueOf(count));
        if (totalValueLabel != null) totalValueLabel.setText(String.format("$%,.0f", totalValue));
        if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(lowStock));
        applyFilter();
    }

    private HBox buildRow(int productId, String id, String name, String category,
                          String stock, String unitPrice, String price, String supplier) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        double[] widths = {110, 200, 130, 100, 110, 100, 150, 80};
        String[] values = {id, name, category, stock, unitPrice, price, supplier};
        String[] styles = {"table-cell", "table-cell", "category-badge", "table-cell", "table-cell", "table-cell", "table-cell"};

        for (int i = 0; i < values.length; i++) {
            if (i == 2) {
                // wrap the badge so the gray background doesn't stretch across the whole column
                Label badge = new Label(values[i]);
                badge.getStyleClass().add("category-badge");
                HBox cell = new HBox(badge);
                cell.setPrefWidth(widths[i]);
                cell.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(cell);
            } else {
                Label lbl = new Label(values[i]);
                lbl.getStyleClass().add(styles[i]);
                lbl.setPrefWidth(widths[i]);
                row.getChildren().add(lbl);
            }
        }

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        editBtn.setPrefWidth(widths[7]);
        editBtn.setOnAction(e -> {
            editingId = productId;
            formTitleLabel.setText("Edit Product");
            nameField.setText(name);
            categoryField.setText(category);
            stockField.setText(stock);
            unitPriceField.setText(unitPrice);
            priceField.setText(price);
            supplierField.setText(supplier);
            addProductForm.setVisible(true);
            addProductForm.setManaged(true);
        });
        row.getChildren().add(editBtn);

        return row;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    private void applyFilter() {
        productRows.getChildren().clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (RowData rowData : allRows) {
            if (query.isEmpty() || rowData.searchableText.contains(query)) {
                productRows.getChildren().add(rowData.row);
            }
        }
    }

    private String idFromDb(int id) {
        return "PRD-" + String.format("%03d", id);
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
