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

class SupplierDataRepositoryValidationTest {

    @Test
    void createOrderRejectsZeroQuantity(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "supplier" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        SupplierDataRepository repository = new SupplierDataRepository();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.createOrder(new SupplierDataRepository.SupplierOrderInput("Acme", "Widget", 0, "", 1, ""))
        );

        assertEquals("Order quantity must be greater than zero.", exception.getMessage());
        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM SupplierOrders")) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }

        UserSession.getInstance().clear();
    }
}
