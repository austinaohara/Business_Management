package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DashboardDataRepository {

    public DashboardData loadDashboard() {
        return new DashboardData(
                loadStats(),
                loadLowStockItems(),
                loadUpcomingShipments(),
                loadTopSellingProduct()
        );
    }

    private DashboardStats loadStats() {
        double totalRevenue = 0;
        int unprocessedOrders = 0;
        int upcomingShipments = 0;
        int lowStockCount = 0;

        try (Connection conn = DatabaseManager.getUserConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM Orders")) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM Orders WHERE status='Pending' OR status='Processing'")) {
                if (rs.next()) {
                    unprocessedOrders = rs.getInt(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM SupplierOrders WHERE status='Pending'")) {
                if (rs.next()) {
                    upcomingShipments = rs.getInt(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM Inventory WHERE quantity_on_hand < minimum_stock")) {
                if (rs.next()) {
                    lowStockCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DashboardStats(totalRevenue, unprocessedOrders, upcomingShipments, lowStockCount);
    }

    private List<LowStockItem> loadLowStockItems() {
        List<LowStockItem> items = new ArrayList<>();

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name, quantity_on_hand, minimum_stock FROM Inventory " +
                             "WHERE quantity_on_hand < minimum_stock " +
                             "ORDER BY quantity_on_hand FETCH FIRST 5 ROWS ONLY")) {
            while (rs.next()) {
                items.add(new LowStockItem(
                        rs.getString("name"),
                        rs.getInt("quantity_on_hand"),
                        rs.getInt("minimum_stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    private List<UpcomingShipment> loadUpcomingShipments() {
        List<UpcomingShipment> shipments = new ArrayList<>();

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT supplier_name, product_name, due_date FROM SupplierOrders " +
                             "WHERE status='Pending' ORDER BY due_date FETCH FIRST 5 ROWS ONLY")) {
            while (rs.next()) {
                shipments.add(new UpcomingShipment(
                        rs.getString("supplier_name"),
                        rs.getString("product_name"),
                        rs.getString("due_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return shipments;
    }

    private TopSellingProduct loadTopSellingProduct() {
        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT product_name, SUM(quantity) AS total_sold FROM OrderItems " +
                             "GROUP BY product_name ORDER BY total_sold DESC FETCH FIRST 1 ROW ONLY")) {
            if (rs.next()) {
                return new TopSellingProduct(
                        rs.getString("product_name"),
                        rs.getInt("total_sold")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public record DashboardData(
            DashboardStats stats,
            List<LowStockItem> lowStockItems,
            List<UpcomingShipment> upcomingShipments,
            TopSellingProduct topSellingProduct
    ) {
    }

    public record DashboardStats(
            double totalRevenue,
            int unprocessedOrders,
            int upcomingShipments,
            int lowStockCount
    ) {
    }

    public record LowStockItem(
            String name,
            int quantityOnHand,
            int minimumStock
    ) {
    }

    public record UpcomingShipment(
            String supplierName,
            String productName,
            String dueDate
    ) {
    }

    public record TopSellingProduct(
            String productName,
            int totalSold
    ) {
    }
}
