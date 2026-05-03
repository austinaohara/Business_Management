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

            createTable(stmt, "CREATE TABLE Inventory (" +
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

            createTable(stmt, "CREATE TABLE Suppliers (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "name VARCHAR(255), " +
                    "contact_info VARCHAR(255), " +
                    "lead_time INT, " +
                    "payment_info VARCHAR(255))");

            createTable(stmt, "CREATE TABLE Customers (" +
                    "profile_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "first_name VARCHAR(100), " +
                    "last_name VARCHAR(100), " +
                    "email VARCHAR(255), " +
                    "phone VARCHAR(20))");

            createTable(stmt, "CREATE TABLE Orders (" +
                    "order_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "customer_id INT, " +
                    "amount DOUBLE, " +
                    "status VARCHAR(50), " +
                    "order_date DATE)");

            createTable(stmt, "CREATE TABLE OrderItems (" +
                    "item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "order_id INT, " +
                    "product_name VARCHAR(255), " +
                    "quantity INT, " +
                    "unit_price DOUBLE)");

            createTable(stmt, "CREATE TABLE StaffProfiles (" +
                    "staff_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password_hash VARCHAR(255), " +
                    "theme_preference VARCHAR(20) DEFAULT 'LIGHT')");

            createTable(stmt, "CREATE TABLE SupplierOrders (" +
                    "order_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "supplier_name VARCHAR(255), " +
                    "product_name VARCHAR(255), " +
                    "quantity INT, " +
                    "due_date VARCHAR(50), " +
                    "priority INT, " +
                    "notes VARCHAR(500), " +
                    "status VARCHAR(50) DEFAULT 'Pending')");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private static void createTable(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                System.err.println("Error creating table: " + e.getMessage());
            }
        }
    }
}