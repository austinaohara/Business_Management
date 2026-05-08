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

class SupplierDataRepositoryTest {

    @Test
    void loadSupplierDataUsesEarliestDueDateForNextDelivery(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "supplier" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,notes,status) " +
                            "VALUES('Acme','Late Item',5,'2026-06-10',1,'','Pending')"
            );
            st.executeUpdate(
                    "INSERT INTO SupplierOrders(supplier_name,product_name,quantity,due_date,priority,notes,status) " +
                            "VALUES('Acme','Soon Item',5,'2026-05-15',1,'','Pending')"
            );
        }

        SupplierDataRepository.SupplierStats stats = new SupplierDataRepository().loadSupplierData().stats();
        assertEquals("2026-05-15", stats.nextDelivery());

        UserSession.getInstance().clear();
    }
}
