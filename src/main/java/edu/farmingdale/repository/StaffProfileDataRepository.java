package edu.farmingdale.repository;

import edu.farmingdale.DatabaseManager;
import edu.farmingdale.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffProfileDataRepository {

    public boolean usernameExists(String username) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM StaffProfiles WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO StaffProfiles(username, password_hash, theme_preference) VALUES(?,?,?)")) {
            ps.setString(1, username);
            ps.setString(2, PasswordHasher.hash(password));
            ps.setString(3, "LIGHT");
            ps.executeUpdate();
            DatabaseManager.initializeUserDatabase(username);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidCredentials(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT password_hash FROM StaffProfiles WHERE username = ?")) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String storedPassword = rs.getString("password_hash");
                if (storedPassword == null) {
                    return false;
                }

                if (PasswordHasher.isHashFormat(storedPassword)) {
                    return PasswordHasher.matches(password, storedPassword);
                }

                boolean matched = storedPassword.equals(password);
                if (matched) {
                    upgradeLegacyPassword(conn, username, password);
                }
                return matched;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void upgradeLegacyPassword(Connection conn, String username, String password) {
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE StaffProfiles SET password_hash = ? WHERE username = ?")) {
            update.setString(1, PasswordHasher.hash(password));
            update.setString(2, username);
            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
