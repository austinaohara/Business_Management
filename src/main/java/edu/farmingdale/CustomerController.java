package edu.farmingdale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;

public class CustomerController {

    @FXML private VBox addCustomerForm;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField;
    @FXML private VBox customerRows;
    @FXML private HBox templateRow;
    @FXML private Label totalCustomersLabel;

    @FXML
    public void initialize() {
        TextFieldFormatter.applyPhoneFormatter(phoneField);
        loadCustomers();
    }

    @FXML
    private void onAddCustomer() {
        addCustomerForm.setVisible(true);
        addCustomerForm.setManaged(true);
    }

    @FXML
    private void onCancelCustomer() {
        addCustomerForm.setVisible(false);
        addCustomerForm.setManaged(false);
        firstNameField.clear(); lastNameField.clear(); emailField.clear(); phoneField.clear();
    }

    @FXML
    private void onSaveCustomer() {
        if (firstNameField.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Customers(first_name,last_name,email,phone) VALUES(?,?,?,?)")) {
            ps.setString(1, firstNameField.getText().trim());
            ps.setString(2, lastNameField.getText().trim());
            ps.setString(3, emailField.getText().trim());
            ps.setString(4, phoneField.getText().trim());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        onCancelCustomer();
        loadCustomers();
    }

    private void loadCustomers() {
        customerRows.getChildren().clear();
        int count = 0;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT profile_id,first_name,last_name,email,phone FROM Customers")) {
            while (rs.next()) {
                count++;
                HBox row = buildRow(
                    "CST-" + String.format("%03d", rs.getInt("profile_id")),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email") != null ? rs.getString("email") : "",
                    rs.getString("phone") != null ? rs.getString("phone") : ""
                );
                customerRows.getChildren().add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (totalCustomersLabel != null) totalCustomersLabel.setText(String.valueOf(count));
    }

    private HBox buildRow(String id, String fullName, String email, String phone) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        double[] widths = {100, 200, 220, 140, 80};
        String[] values = {id, fullName, email, phone, ""};
        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add("table-cell");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }
}
