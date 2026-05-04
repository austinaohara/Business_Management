package edu.farmingdale;

import edu.farmingdale.repository.DashboardDataRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardController implements Refreshable {

    @FXML private Label totalRevenueLabel;
    @FXML private Label unprocessedOrdersLabel;
    @FXML private Label upcomingShipmentsLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private VBox lowStockRows;
    @FXML private VBox shipmentRows;
    @FXML private Label topProductLabel;
    @FXML private Label topProductSalesLabel;

    private final DashboardDataRepository dashboardRepository = new DashboardDataRepository();

    @FXML
    public void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        DashboardDataRepository.DashboardData dashboardData = dashboardRepository.loadDashboard();
        loadStats(dashboardData.stats());
        loadLowStockItems(dashboardData.lowStockItems());
        loadUpcomingShipments(dashboardData.upcomingShipments());
        loadTopSellingProduct(dashboardData.topSellingProduct());
    }

    private void loadStats(DashboardDataRepository.DashboardStats stats) {
        totalRevenueLabel.setText(
                NumberFormat.getCurrencyInstance(Locale.US).format(stats.totalRevenue()));
        unprocessedOrdersLabel.setText(String.valueOf(stats.unprocessedOrders()));
        upcomingShipmentsLabel.setText(String.valueOf(stats.upcomingShipments()));
        lowStockCountLabel.setText(String.valueOf(stats.lowStockCount()));
        if (stats.lowStockCount() > 0) {
            lowStockCountLabel.getStyleClass().remove("stat-value");
            lowStockCountLabel.getStyleClass().add("stat-value-warning");
        }
    }

    private void loadLowStockItems(Iterable<DashboardDataRepository.LowStockItem> items) {
        lowStockRows.getChildren().clear();
        for (DashboardDataRepository.LowStockItem item : items) {
            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(cell(item.name(), 200));
            Label qtyLabel = new Label(String.valueOf(item.quantityOnHand()));
            qtyLabel.getStyleClass().addAll("table-cell", "low-stock-text");
            qtyLabel.setPrefWidth(100);
            row.getChildren().add(qtyLabel);
            row.getChildren().add(cell("Min: " + item.minimumStock(), 100));
            lowStockRows.getChildren().add(row);
        }
        if (lowStockRows.getChildren().isEmpty()) {
            lowStockRows.getChildren().add(emptyLabel("All products are sufficiently stocked."));
        }
    }

    private void loadUpcomingShipments(Iterable<DashboardDataRepository.UpcomingShipment> shipments) {
        shipmentRows.getChildren().clear();
        for (DashboardDataRepository.UpcomingShipment shipment : shipments) {
            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(cell(shipment.supplierName(), 160));
            row.getChildren().add(cell(shipment.productName(), 160));
            row.getChildren().add(cell(shipment.dueDate() != null ? shipment.dueDate() : "â€”", 100));
            shipmentRows.getChildren().add(row);
        }
        if (shipmentRows.getChildren().isEmpty()) {
            shipmentRows.getChildren().add(emptyLabel("No pending shipments."));
        }
    }

    private void loadTopSellingProduct(DashboardDataRepository.TopSellingProduct topSellingProduct) {
        if (topSellingProduct == null) {
            topProductLabel.setText("No sales data yet.");
            topProductSalesLabel.setText("");
            return;
        }
        topProductLabel.setText(topSellingProduct.productName());
        topProductSalesLabel.setText(topSellingProduct.totalSold() + " units sold");
    }

    private Label cell(String text, double width) {
        Label lbl = new Label(text != null ? text : "â€”");
        lbl.getStyleClass().add("table-cell");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private Label emptyLabel(String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");
        return lbl;
    }
}
