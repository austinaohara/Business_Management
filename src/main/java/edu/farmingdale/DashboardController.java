package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
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

    @FXML
    public void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        loadStats();
        loadLowStockItems();
        loadUpcomingShipments();
        loadTopSellingProduct();
    }

    private void loadStats() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM Orders")) {
                if (rs.next()) {
                    totalRevenueLabel.setText(
                        NumberFormat.getCurrencyInstance(Locale.US).format(rs.getDouble(1)));
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM Orders WHERE status='Pending' OR status='Processing'")) {
                if (rs.next()) unprocessedOrdersLabel.setText(String.valueOf(rs.getInt(1)));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM SupplierOrders WHERE status='Pending'")) {
                if (rs.next()) upcomingShipmentsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM Inventory WHERE quantity_on_hand < minimum_stock")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    lowStockCountLabel.setText(String.valueOf(count));
                    if (count > 0) {
                        lowStockCountLabel.getStyleClass().remove("stat-value");
                        lowStockCountLabel.getStyleClass().add("stat-value-warning");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLowStockItems() {
        lowStockRows.getChildren().clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT name, quantity_on_hand, minimum_stock FROM Inventory " +
                 "WHERE quantity_on_hand < minimum_stock " +
                 "ORDER BY quantity_on_hand FETCH FIRST 5 ROWS ONLY")) {
            while (rs.next()) {
                HBox row = new HBox();
                row.getStyleClass().add("table-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(cell(rs.getString("name"), 200));
                Label qtyLabel = new Label(String.valueOf(rs.getInt("quantity_on_hand")));
                qtyLabel.getStyleClass().addAll("table-cell", "low-stock-text");
                qtyLabel.setPrefWidth(100);
                row.getChildren().add(qtyLabel);
                row.getChildren().add(cell("Min: " + rs.getInt("minimum_stock"), 100));
                lowStockRows.getChildren().add(row);
            }
            if (lowStockRows.getChildren().isEmpty()) {
                lowStockRows.getChildren().add(emptyLabel("All products are sufficiently stocked."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUpcomingShipments() {
        shipmentRows.getChildren().clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT supplier_name, product_name, due_date FROM SupplierOrders " +
                 "WHERE status='Pending' ORDER BY due_date FETCH FIRST 5 ROWS ONLY")) {
            while (rs.next()) {
                HBox row = new HBox();
                row.getStyleClass().add("table-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(cell(rs.getString("supplier_name"), 160));
                row.getChildren().add(cell(rs.getString("product_name"), 160));
                String due = rs.getString("due_date");
                row.getChildren().add(cell(due != null ? due : "—", 100));
                shipmentRows.getChildren().add(row);
            }
            if (shipmentRows.getChildren().isEmpty()) {
                shipmentRows.getChildren().add(emptyLabel("No pending shipments."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTopSellingProduct() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT product_name, SUM(quantity) AS total_sold FROM OrderItems " +
                 "GROUP BY product_name ORDER BY total_sold DESC FETCH FIRST 1 ROW ONLY")) {
            if (rs.next()) {
                topProductLabel.setText(rs.getString("product_name"));
                topProductSalesLabel.setText(rs.getInt("total_sold") + " units sold");
            } else {
                topProductLabel.setText("No sales data yet.");
                topProductSalesLabel.setText("");
            }
        } catch (SQLException e) {
            topProductLabel.setText("No sales data yet.");
            topProductSalesLabel.setText("");
        }
    }

    private Label cell(String text, double width) {
        Label lbl = new Label(text != null ? text : "—");
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
