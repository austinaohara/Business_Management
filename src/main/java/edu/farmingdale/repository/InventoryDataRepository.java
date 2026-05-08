package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InventoryDataRepository {

    public InventoryViewData loadInventory() {
        List<InventoryProductRow> products = new ArrayList<>();
        int totalProducts = 0;
        double totalValue = 0;
        int lowStockCount = 0;

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name, category, quantity_on_hand, minimum_stock, unit_cost, sell_price, supplier FROM Inventory")) {
            while (rs.next()) {
                totalProducts++;

                int id = rs.getInt("id");
                int quantityOnHand = rs.getInt("quantity_on_hand");
                int minimumStock = rs.getInt("minimum_stock");
                double displayedUnitPrice = rs.getDouble("unit_cost");
                double sellPrice = rs.getDouble("sell_price");

                totalValue += quantityOnHand * displayedUnitPrice;
                if (quantityOnHand <= minimumStock) {
                    lowStockCount++;
                }

                products.add(new InventoryProductRow(
                        id,
                        idFromDb(id),
                        rs.getString("name"),
                        rs.getString("category"),
                        quantityOnHand,
                        minimumStock,
                        displayedUnitPrice,
                        sellPrice,
                        safeText(rs.getString("supplier"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new InventoryViewData(products, totalProducts, totalValue, lowStockCount);
    }

    public void saveProduct(InventoryProductInput input) {
        try (Connection conn = DatabaseManager.getUserConnection()) {
            if (input.id() != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Inventory SET name=?,category=?,quantity_on_hand=?,unit_cost=?,sell_price=?,supplier=? WHERE id=?")) {
                    ps.setString(1, input.name());
                    ps.setString(2, input.category());
                    ps.setInt(3, input.quantityOnHand());
                    ps.setDouble(4, input.unitCost());
                    ps.setDouble(5, input.sellPrice());
                    ps.setString(6, input.supplier());
                    ps.setInt(7, input.id());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,unit_cost,sell_price,supplier) VALUES(?,?,?,?,?,?,?)")) {
                    ps.setString(1, input.name());
                    ps.setString(2, input.category());
                    ps.setInt(3, input.quantityOnHand());
                    ps.setInt(4, 0);
                    ps.setDouble(5, input.unitCost());
                    ps.setDouble(6, input.sellPrice());
                    ps.setString(7, input.supplier());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String idFromDb(int id) {
        return "PRD-" + String.format("%03d", id);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public record InventoryProductInput(
            Integer id,
            String name,
            String category,
            int quantityOnHand,
            double unitCost,
            double sellPrice,
            String supplier
    ) {
    }

    public record InventoryProductRow(
            int id,
            String productCode,
            String name,
            String category,
            int quantityOnHand,
            int minimumStock,
            double displayedUnitPrice,
            double sellPrice,
            String supplier
    ) {
    }

    public record InventoryViewData(
            List<InventoryProductRow> products,
            int totalProducts,
            double totalValue,
            int lowStockCount
    ) {
    }
}
