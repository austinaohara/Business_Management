package edu.farmingdale;

import edu.farmingdale.util.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String MASTER_DB_URL = "jdbc:derby:BusinessManagementDB;create=true";

    /** Master DB — StaffProfiles auth only. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MASTER_DB_URL);
    }

    /** Per-user DB — all operational tables. */
    public static Connection getUserConnection() throws SQLException {
        var user = UserSession.getInstance().getCurrentUser();
        if (user == null) {
            throw new SQLException("No user is currently logged in.");
        }
        return DriverManager.getConnection(user.getDbUrl());
    }

    /** Called once at startup — creates the master StaffProfiles table only. */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            createTable(stmt, "CREATE TABLE StaffProfiles (" +
                    "staff_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password_hash VARCHAR(255), " +
                    "theme_preference VARCHAR(20) DEFAULT 'LIGHT')");

            ensureDefaultStaffProfile(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /** Called on registration and on first login — creates all operational tables in the user's own DB. */
    public static void initializeUserDatabase(String username) {
        String userDbUrl = "jdbc:derby:BusinessManagementDB_" + username + ";create=true";
        try (Connection conn = DriverManager.getConnection(userDbUrl);
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
            System.err.println("User database initialization error: " + e.getMessage());
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

    private static void ensureDefaultStaffProfile(Connection conn) {
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE StaffProfiles SET password_hash = ?, theme_preference = ? WHERE username = ?")) {
            update.setString(1, PasswordHasher.hash("password"));
            update.setString(2, "LIGHT");
            update.setString(3, "employee");
            if (update.executeUpdate() > 0) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error updating default staff profile: " + e.getMessage());
            return;
        }

        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO StaffProfiles(username, password_hash, theme_preference) VALUES(?,?,?)")) {
            insert.setString(1, "employee");
            insert.setString(2, PasswordHasher.hash("password"));
            insert.setString(3, "LIGHT");
            insert.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating default staff profile: " + e.getMessage());
        }
    }

    /** Creates a backup of the operational database for disaster recovery */
    public static void backupUserDatabase(String username) {
        try {
            String backupPath = System.getProperty("user.home") + "/BusinessManagementBackups/" + username + "_backup";
            java.io.File backupDir = new java.io.File(backupPath);
            backupDir.mkdirs();

            try (Connection conn = getUserConnection();
                 java.sql.CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)")) {
                cs.setString(1, backupPath);
                cs.execute();
                System.out.println("Database successfully backed up to: " + backupPath);
            }
        } catch (Exception e) {
            System.err.println("Database backup failed: " + e.getMessage());
        }
    }
}
