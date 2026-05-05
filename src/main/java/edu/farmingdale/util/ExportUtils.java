package edu.farmingdale.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class ExportUtils {

    /**
     * Opens a save dialog and exports the provided data to a CSV file.
     *
     * @param window          The current application window
     * @param defaultFileName The suggested name for the file (e.g., "inventory_report.csv")
     * @param headers         An array of column headers
     * @param data            A list of string arrays representing the rows of data
     */
    public static void exportToCSV(Window window, String defaultFileName, String[] headers, List<String[]> data) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Writes the header row
                writer.println(String.join(",", headers));

                // Writes each data row
                for (String[] row : data) {
                    // Wraps fields containing commas in quotes so they don't break the CSV
                    for (int i = 0; i < row.length; i++) {
                        if (row[i] != null && row[i].contains(",")) {
                            row[i] = "\"" + row[i] + "\"";
                        }
                    }
                    writer.println(String.join(",", row));
                }
                System.out.println("Export successful: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error exporting to CSV: " + e.getMessage());
            }
        }
    }
}