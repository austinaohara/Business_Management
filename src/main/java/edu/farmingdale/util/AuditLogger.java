package edu.farmingdale.util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {
    private static final String LOG_FILE = "system_audit.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logAction(String user, String action, String details) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            out.printf("[%s] USER: %s | ACTION: %s | DETAILS: %s%n", timestamp, user, action, details);
        } catch (Exception e) {
            System.err.println("Failed to write to audit log: " + e.getMessage());
        }
    }
}