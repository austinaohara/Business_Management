package edu.farmingdale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:derby:BusinessManagementDB;create=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Inventory table
            stmt.execute("CREATE TABLE Inventory (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "name VARCHAR(255), " +
                    "category VARCHAR(100), " +
                    "description VARCHAR(255), " +
                    "unit_cost DOUBLE, " +
                    "sell_price DOUBLE, " +
                    "quantity_on_hand INT, " +
                    "minimum_stock INT, " +
                    "supplier VARCHAR(255), " +
                    "storage_location VARCHAR(100))");

            // Suppliers table
            stmt.execute("CREATE TABLE Suppliers (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "name VARCHAR(255), " +
                    "contact_info VARCHAR(255), " +
                    "lead_time INT, " +
                    "payment_info VARCHAR(255))");

            // Customers table
            stmt.execute("CREATE TABLE Customers (" +
                    "profile_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "first_name VARCHAR(100), " +
                    "last_name VARCHAR(100), " +
                    "email VARCHAR(255), " +
                    "phone VARCHAR(20))");

            // Orders table
            stmt.execute("CREATE TABLE Orders (" +
                    "order_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "customer_id INT, " +
                    "amount DOUBLE, " +
                    "status VARCHAR(50), " +
                    "order_date DATE)");

            // Staff profiles
            stmt.execute("CREATE TABLE StaffProfiles (" +
                    "staff_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password_hash VARCHAR(255), " +
                    "theme_preference VARCHAR(20) DEFAULT 'LIGHT')");

            System.out.println("Database tables created successfully.");

        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                System.err.println("Database initialization error: " + e.getMessage());
            }
        }
    }
}