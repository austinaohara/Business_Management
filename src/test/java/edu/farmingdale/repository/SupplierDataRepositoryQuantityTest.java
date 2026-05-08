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

class SupplierDataRepositoryQuantityTest {

    @Test
    void confirmDeliveryRejectsZeroQuantity(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "confirmqty" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,notes,status) " +
                            "VALUES('Acme','Widget',3,'2026-05-20',1,'','Pending')"
            );
        }

        SupplierDataRepository repository = new SupplierDataRepository();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.confirmDelivery(1, "Widget", "Acme", 0)
        );

        assertEquals("Delivery quantity must be greater than zero.", exception.getMessage());
        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT status FROM SupplierOrders WHERE order_id=1")) {
            rs.next();
            assertEquals("Pending", rs.getString(1));
        }

        UserSession.getInstance().clear();
    }
}
