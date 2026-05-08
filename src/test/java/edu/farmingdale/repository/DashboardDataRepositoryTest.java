package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;
import edu.farmingdale.UserSession;
import edu.farmingdale.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardDataRepositoryTest {

    @Test
    void lowStockMetricsIncludeItemsAtMinimumStock(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "dashboard" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,unit_cost,sell_price,supplier) " +
                            "VALUES('Borderline','Hardware',5,5,1.00,2.00,'Acme')"
            );
        }

        assertEquals(1, new InventoryDataRepository().loadInventory().lowStockCount());
        DashboardDataRepository.DashboardData dashboard = new DashboardDataRepository().loadDashboard();
        assertEquals(1, dashboard.stats().lowStockCount());
        assertEquals("Borderline", dashboard.lowStockItems().getFirst().name());

        UserSession.getInstance().clear();
    }
}
