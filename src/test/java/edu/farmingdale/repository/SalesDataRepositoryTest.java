package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;
import edu.farmingdale.UserSession;
import edu.farmingdale.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesDataRepositoryTest {

    @Test
    void recordSaleRollsBackWhenRequestedQuantityExceedsStock(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "sales" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO Inventory(name,category,quantity_on_hand,minimum_stock,unit_cost,sell_price,supplier) " +
                            "VALUES('Widget','Hardware',2,0,2.50,5.00,'Acme')"
            );
        }

        SalesDataRepository repository = new SalesDataRepository();
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.recordSale(new SalesDataRepository.SaleInput("Widget", 3, 5.00))
        );

        assertEquals("Quantity exceeds available stock.", exception.getMessage());
        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            try (ResultSet orderRs = st.executeQuery("SELECT COUNT(*) FROM Orders")) {
                orderRs.next();
                assertEquals(0, orderRs.getInt(1));
            }
            try (ResultSet stockRs = st.executeQuery("SELECT quantity_on_hand FROM Inventory WHERE name='Widget'")) {
                stockRs.next();
                assertEquals(2, stockRs.getInt(1));
            }
        } finally {
            UserSession.getInstance().clear();
        }
    }
}
