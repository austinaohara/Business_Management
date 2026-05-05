package edu.farmingdale;

import edu.farmingdale.repository.InventoryDataRepository;
import edu.farmingdale.util.ExportUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryController implements Refreshable {

    @FXML private VBox addProductForm;
    @FXML private Label formTitleLabel;
    @FXML private TextField nameField, categoryField, stockField, unitPriceField, priceField, supplierField;
    @FXML private TextField searchField;
    @FXML private VBox productRows;
    @FXML private HBox templateRow;
    @FXML private Label totalProductsLabel, totalValueLabel, lowStockLabel;

    private final List<RowData> allRows = new ArrayList<>();
    private final InventoryDataRepository inventoryRepository = new InventoryDataRepository();

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

    @Override
    public void refresh() {
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
        if (nameField.getText().trim().isEmpty()) {
            showWarning("Please enter a product name.");
            return;
        }
        inventoryRepository.saveProduct(new InventoryDataRepository.InventoryProductInput(
                editingId >= 0 ? editingId : null,
                nameField.getText().trim(),
                categoryField.getText().trim(),
                parseIntSafe(stockField.getText()),
                parseDoubleSafe(unitPriceField.getText()),
                parseDoubleSafe(priceField.getText()),
                supplierField.getText().trim()
        ));
        onCancelProduct();
        loadProducts();
    }

    @FXML
    private void handleExportInventory(ActionEvent event) {
        InventoryDataRepository.InventoryViewData inventoryData = inventoryRepository.loadInventory();

        String[] headers = {"Product Code", "Name", "Category", "Quantity on Hand", "Unit Price", "Sell Price", "Supplier"};

        List<String[]> data = inventoryData.products().stream().map(p -> new String[]{
                safeText(p.productCode()),
                safeText(p.name()),
                safeText(p.category()),
                String.valueOf(p.quantityOnHand()),
                String.format("%.2f", p.displayedUnitPrice()),
                String.format("%.2f", p.sellPrice()),
                safeText(p.supplier())
        }).collect(Collectors.toList());

        Window window = ((Node) event.getSource()).getScene().getWindow();
        ExportUtils.exportToCSV(window, "Inventory_Stock_Report.csv", headers, data);
    }

    private void loadProducts() {
        allRows.clear();
        InventoryDataRepository.InventoryViewData inventoryData = inventoryRepository.loadInventory();
        for (InventoryDataRepository.InventoryProductRow product : inventoryData.products()) {
            HBox row = buildRow(product);
            String searchable = String.join(" ",
                    product.productCode(),
                    safeText(product.name()),
                    safeText(product.category()),
                    safeText(product.supplier())
            ).toLowerCase();
            allRows.add(new RowData(searchable, row));
        }
        if (totalProductsLabel != null) totalProductsLabel.setText(String.valueOf(inventoryData.totalProducts()));
        if (totalValueLabel != null) totalValueLabel.setText(String.format("$%,.0f", inventoryData.totalValue()));
        if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(inventoryData.lowStockCount()));
        applyFilter();
    }

    private HBox buildRow(InventoryDataRepository.InventoryProductRow product) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        double[] widths = {110, 200, 130, 100, 110, 100, 150, 80};
        String stock = String.valueOf(product.quantityOnHand());
        String unitPrice = String.format("%.2f", product.displayedUnitPrice());
        String price = String.format("%.2f", product.sellPrice());
        String[] values = {
                product.productCode(),
                product.name(),
                product.category(),
                stock,
                unitPrice,
                price,
                product.supplier()
        };
        String[] styles = {"table-cell", "table-cell", "category-badge", "table-cell", "table-cell", "table-cell", "table-cell"};

        for (int i = 0; i < values.length; i++) {
            if (i == 2) {
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
            editingId = product.id();
            formTitleLabel.setText("Edit Product");
            nameField.setText(product.name());
            categoryField.setText(product.category());
            stockField.setText(stock);
            unitPriceField.setText(unitPrice);
            priceField.setText(price);
            supplierField.setText(product.supplier());
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Missing Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
