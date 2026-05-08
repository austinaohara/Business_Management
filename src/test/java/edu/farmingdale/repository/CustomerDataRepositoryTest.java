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

class CustomerDataRepositoryTest {

    @Test
    void saveCustomerRejectsBlankLastName(@TempDir Path tempDir) throws Exception {
        System.setProperty("derby.system.home", tempDir.toString());
        String username = "customer" + System.nanoTime();
        UserSession.getInstance().setCurrentUser(new User(username));
        DatabaseManager.initializeUserDatabase(username);

        CustomerDataRepository repository = new CustomerDataRepository();
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveCustomer(new CustomerDataRepository.CustomerInput("Jane", " ", "", ""))
        );

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Customers")) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }

        UserSession.getInstance().clear();
    }
}
