package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalesDataRepository {

    public List<AvailableProduct> findAvailableProducts() {
        List<AvailableProduct> products = new ArrayList<>();

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name, sell_price, quantity_on_hand FROM Inventory " +
                             "WHERE quantity_on_hand > 0 ORDER BY name")) {
            while (rs.next()) {
                products.add(new AvailableProduct(
                        rs.getString("name"),
                        rs.getDouble("sell_price"),
                        rs.getInt("quantity_on_hand")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public void recordSale(SaleInput input) {
        try (Connection conn = DatabaseManager.getUserConnection()) {
            conn.setAutoCommit(false);

            try {
                if (input.quantity() <= 0) {
                    throw new IllegalArgumentException("Sale quantity must be greater than zero.");
                }
                if (input.unitPrice() <= 0) {
                    throw new IllegalArgumentException("Sale price must be greater than zero.");
                }

                try (PreparedStatement inventoryPs = conn.prepareStatement(
                        "UPDATE Inventory SET quantity_on_hand = quantity_on_hand - ? " +
                                "WHERE UPPER(name) = UPPER(?) AND quantity_on_hand >= ?")) {
                    inventoryPs.setInt(1, input.quantity());
                    inventoryPs.setString(2, input.productName());
                    inventoryPs.setInt(3, input.quantity());
                    if (inventoryPs.executeUpdate() == 0) {
                        throw buildSaleValidationException(conn, input.productName());
                    }
                }

            int orderId;

            try (PreparedStatement orderPs = conn.prepareStatement(
                    "INSERT INTO Orders(customer_id, amount, status, order_date) VALUES(NULL,?,?,CURRENT_DATE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                orderPs.setDouble(1, input.quantity() * input.unitPrice());
                orderPs.setString(2, "Completed");
                orderPs.executeUpdate();

                try (ResultSet keys = orderPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Unable to create sale order.");
                    }
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement itemPs = conn.prepareStatement(
                    "INSERT INTO OrderItems(order_id, product_name, quantity, unit_price) VALUES(?,?,?,?)")) {
                itemPs.setInt(1, orderId);
                itemPs.setString(2, input.productName());
                itemPs.setInt(3, input.quantity());
                itemPs.setDouble(4, input.unitPrice());
                itemPs.executeUpdate();
            }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e instanceof RuntimeException
                        ? (RuntimeException) e
                        : new IllegalStateException("Unable to record sale.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to record sale.", e);
        }
    }

    public SalesViewData loadSales() {
        List<SaleRow> sales = new ArrayList<>();
        int totalSales = 0;
        int itemsSold = 0;
        double totalRevenue = 0;

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT o.order_id, oi.product_name, oi.quantity AS qty, oi.unit_price, " +
                             "o.amount, o.order_date, o.status " +
                             "FROM Orders o JOIN OrderItems oi ON o.order_id = oi.order_id " +
                             "ORDER BY o.order_date DESC, o.order_id DESC")) {
            while (rs.next()) {
                totalSales++;
                itemsSold += rs.getInt("qty");
                totalRevenue += rs.getDouble("amount");

                int orderId = rs.getInt("order_id");
                sales.add(new SaleRow(
                        orderId,
                        saleIdFromDb(orderId),
                        rs.getString("product_name"),
                        rs.getInt("qty"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("amount"),
                        rs.getString("order_date"),
                        rs.getString("status") != null ? rs.getString("status") : "Completed"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new SalesViewData(sales, totalRevenue, totalSales, itemsSold);
    }

    private String saleIdFromDb(int orderId) {
        return "ORD-" + String.format("%03d", orderId);
    }

    private RuntimeException buildSaleValidationException(Connection conn, String productName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM Inventory WHERE UPPER(name) = UPPER(?)")) {
            ps.setString(1, productName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new IllegalStateException("Quantity exceeds available stock.")
                        : new IllegalStateException("Product no longer exists.");
            }
        }
    }

    public record AvailableProduct(
            String name,
            double sellPrice,
            int quantityOnHand
    ) {
    }

    public record SaleInput(
            String productName,
            int quantity,
            double unitPrice
    ) {
    }

    public record SaleRow(
            int orderId,
            String saleCode,
            String productName,
            int quantity,
            double unitPrice,
            double amount,
            String orderDate,
            String status
    ) {
    }

    public record SalesViewData(
            List<SaleRow> sales,
            double totalRevenue,
            int totalSales,
            int itemsSold
    ) {
    }
}
