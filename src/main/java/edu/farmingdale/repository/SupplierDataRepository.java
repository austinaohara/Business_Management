package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDataRepository {

    public SupplierViewData loadSupplierData() {
        return new SupplierViewData(loadStats(), loadDeliveries());
    }

    public void createOrder(SupplierOrderInput input) {
        try (Connection conn = DatabaseManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,notes) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, input.supplierName());
            ps.setString(2, input.productName());
            ps.setInt(3, input.quantity());
            ps.setString(4, input.dueDate());
            ps.setInt(5, input.priority());
            ps.setString(6, input.notes());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void confirmDelivery(int orderId, String productName, String supplierName, int quantity) {
        try (Connection conn = DatabaseManager.getUserConnection()) {
            Integer existingId = null;
            Integer existingQuantity = null;

            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT id, quantity_on_hand FROM Inventory WHERE UPPER(name) = UPPER(?)")) {
                check.setString(1, productName);

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                        existingQuantity = rs.getInt("quantity_on_hand");
                    }
                }
            }

            if (existingId != null && existingQuantity != null) {
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE Inventory SET quantity_on_hand=? WHERE id=?")) {
                    update.setInt(1, existingQuantity + quantity);
                    update.setInt(2, existingId);
                    update.executeUpdate();
                }
            } else {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,sell_price,supplier) VALUES(?,?,?,?,?,?)")) {
                    insert.setString(1, productName);
                    insert.setString(2, "");
                    insert.setInt(3, quantity);
                    insert.setDouble(4, 0);
                    insert.setDouble(5, 0);
                    insert.setString(6, supplierName);
                    insert.executeUpdate();
                }
            }

            try (PreparedStatement markDone = conn.prepareStatement(
                    "UPDATE SupplierOrders SET status='Delivered' WHERE order_id=?")) {
                markDone.setInt(1, orderId);
                markDone.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SupplierStats loadStats() {
        int activeSuppliers = 0;
        int activeOrders = 0;
        String nextDelivery = null;
        int deliveredCount = 0;

        try (Connection conn = DatabaseManager.getUserConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Suppliers")) {
                if (rs.next()) {
                    activeSuppliers = rs.getInt(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM SupplierOrders WHERE status='Pending'")) {
                if (rs.next()) {
                    activeOrders = rs.getInt(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT due_date FROM SupplierOrders WHERE status='Pending' " +
                                 "AND due_date IS NOT NULL AND due_date <> '' " +
                                 "ORDER BY due_date ASC, order_id ASC FETCH FIRST 1 ROW ONLY")) {
                if (rs.next()) {
                    nextDelivery = rs.getString("due_date");
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM SupplierOrders WHERE status='Delivered'")) {
                if (rs.next()) {
                    deliveredCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new SupplierStats(activeSuppliers, activeOrders, nextDelivery, deliveredCount);
    }

    private List<SupplierDeliveryRow> loadDeliveries() {
        List<SupplierDeliveryRow> deliveries = new ArrayList<>();

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT order_id,supplier_name,product_name,quantity,due_date,priority,notes,status FROM SupplierOrders ORDER BY due_date")) {
            while (rs.next()) {
                deliveries.add(new SupplierDeliveryRow(
                        rs.getInt("order_id"),
                        rs.getString("supplier_name"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getString("due_date"),
                        rs.getInt("priority"),
                        rs.getString("notes"),
                        rs.getString("status") != null ? rs.getString("status") : "Pending"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deliveries;
    }

    public record SupplierOrderInput(
            String supplierName,
            String productName,
            int quantity,
            String dueDate,
            int priority,
            String notes
    ) {
    }

    public record SupplierStats(
            int activeSuppliers,
            int activeOrders,
            String nextDelivery,
            int deliveredCount
    ) {
    }

    public record SupplierDeliveryRow(
            int orderId,
            String supplierName,
            String productName,
            int quantity,
            String dueDate,
            int priority,
            String notes,
            String status
    ) {
    }

    public record SupplierViewData(
            SupplierStats stats,
            List<SupplierDeliveryRow> deliveries
    ) {
    }
}
