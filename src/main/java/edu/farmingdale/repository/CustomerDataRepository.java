package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;
import edu.farmingdale.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDataRepository {

    public CustomerViewData loadCustomers() {
        List<CustomerRow> customers = new ArrayList<>();
        int totalCustomers = 0;

        try (Connection conn = DatabaseManager.getUserConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT profile_id, first_name, last_name, email, phone FROM Customers")) {
            while (rs.next()) {
                totalCustomers++;

                int id = rs.getInt("profile_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                customers.add(new CustomerRow(
                        id,
                        idFromDb(id),
                        firstName,
                        lastName,
                        safeText(rs.getString("email")),
                        safeText(rs.getString("phone"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new CustomerViewData(customers, totalCustomers);
    }

    public void saveCustomer(CustomerInput input) {
        Customer customer = new Customer(
                input.firstName(),
                input.lastName(),
                input.email(),
                input.phone()
        );

        try (Connection conn = DatabaseManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Customers(first_name,last_name,email,phone) VALUES(?,?,?,?)")) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String idFromDb(int id) {
        return "CST-" + String.format("%03d", id);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public record CustomerInput(
            Integer id,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {
    }

    public record CustomerRow(
            int id,
            String customerCode,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {
        public String fullName() {
            return firstName + " " + lastName;
        }
    }

    public record CustomerViewData(
            List<CustomerRow> customers,
            int totalCustomers
    ) {
    }
}
