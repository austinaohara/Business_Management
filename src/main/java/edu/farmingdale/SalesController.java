package edu.farmingdale;

import edu.farmingdale.repository.SalesDataRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private final Map<String, SalesDataRepository.AvailableProduct> availableProducts = new HashMap<>();
    private final SalesDataRepository salesRepository = new SalesDataRepository();

    @FXML
    public void initialize() {
        TextFieldFormatter.applyIntegerFilter(saleQuantityField);
        TextFieldFormatter.applyDecimalFilter(salePriceField);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        }
        saleProductCombo.setOnAction(e -> updateSelectedProduct());
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
        availableProducts.clear();
        saleProductCombo.getItems().clear();
        for (SalesDataRepository.AvailableProduct product : salesRepository.findAvailableProducts()) {
            availableProducts.put(product.name(), product);
            saleProductCombo.getItems().add(product.name());
        }
    }

    private void updateSelectedProduct() {
        String selected = saleProductCombo.getValue();
        if (selected != null) {
            SalesDataRepository.AvailableProduct product = availableProducts.get(selected);
            double price = product != null ? product.sellPrice() : 0.0;
            int available = product != null ? product.quantityOnHand() : 0;
            if (price > 0) {
                salePriceField.setText(String.format("%.2f", price));
            } else {
                salePriceField.clear();
            }
            availableLabel.setText("In stock: " + available + " units");
            availableLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 12px;");
        }
    }

    @FXML
    private void onSaveSale() {
        String product = saleProductCombo.getValue();
        int qty = parseIntSafe(saleQuantityField.getText());
        double unitPrice = parseDoubleSafe(salePriceField.getText());
        if (product == null || product.isEmpty() || qty <= 0 || salePriceField.getText().trim().isEmpty()) return;
        SalesDataRepository.AvailableProduct availableProduct = availableProducts.get(product);
        int available = availableProduct != null ? availableProduct.quantityOnHand() : 0;
        if (qty > available) return;
        salesRepository.recordSale(new SalesDataRepository.SaleInput(product, qty, unitPrice));

        onCancelSale();
        loadSales();
    }

    private void loadSales() {
        allRows.clear();
        SalesDataRepository.SalesViewData salesData = salesRepository.loadSales();
        for (SalesDataRepository.SaleRow sale : salesData.sales()) {
            HBox row = buildRow(sale);
            allRows.add(new RowData(
                    String.join(" ",
                            sale.saleCode(),
                            sale.productName(),
                            sale.orderDate() != null ? sale.orderDate() : "",
                            sale.status()
                    ).toLowerCase(),
                    row
            ));
        }
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        if (totalRevenueLabel != null) totalRevenueLabel.setText(currency.format(salesData.totalRevenue()));
        if (totalSalesLabel != null)   totalSalesLabel.setText(String.valueOf(salesData.totalSales()));
        if (itemsSoldLabel != null)    itemsSoldLabel.setText(String.valueOf(salesData.itemsSold()));
        applyFilter();
    }

    private HBox buildRow(SalesDataRepository.SaleRow sale) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        double[] widths = {110, 200, 70, 110, 110, 110};
        String[] values = {
                sale.saleCode(),
                sale.productName(),
                String.valueOf(sale.quantity()),
                NumberFormat.getCurrencyInstance(Locale.US).format(sale.unitPrice()),
                NumberFormat.getCurrencyInstance(Locale.US).format(sale.amount()),
                sale.orderDate() != null ? sale.orderDate() : ""
        };
        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add("table-cell");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        Label statusLabel = new Label(sale.status());
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
