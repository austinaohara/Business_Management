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

class InventoryDataRepositoryTest {

    @Test
    void loadInventoryCalculatesTotalValueFromUnitCost(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "inventory" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,unit_cost,sell_price,supplier) " +
                            "VALUES('Widget','Hardware',4,1,2.50,9.99,'Acme')"
            );
        }

        InventoryDataRepository.InventoryViewData inventory = new InventoryDataRepository().loadInventory();
        assertEquals(10.0, inventory.totalValue());

        UserSession.getInstance().clear();
    }
}
