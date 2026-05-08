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

class SalesDataRepositoryValidationTest {

    @Test
    void recordSaleRejectsZeroPrice(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "saleprice" + System.nanoTime();
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
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.recordSale(new SalesDataRepository.SaleInput("Widget", 1, 0.0))
        );

        assertEquals("Sale price must be greater than zero.", exception.getMessage());
        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Orders")) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }

        UserSession.getInstance().clear();
    }
}
