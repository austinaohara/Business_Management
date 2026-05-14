package edu.farmingdale;

import edu.farmingdale.repository.CustomerDataRepository;
import edu.farmingdale.util.ExportUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Window;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController implements Refreshable {

    @FXML private VBox addCustomerForm;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField;
    @FXML private TextField searchField;
    @FXML private VBox customerRows;
    @FXML private HBox templateRow;
    @FXML private Label totalCustomersLabel;

    private final List<RowData> allRows = new ArrayList<>();
    private final CustomerDataRepository customerRepository = new CustomerDataRepository();
    private int editingId = -1;

    @FXML
    public void initialize() {
        TextFieldFormatter.applyPhoneFormatter(phoneField);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        }
        loadCustomers();
    }

    @Override
    public void refresh() {
        loadCustomers();
    }

    @FXML
    private void onAddCustomer() {
        editingId = -1;
        addCustomerForm.setVisible(true);
        addCustomerForm.setManaged(true);
    }

    @FXML
    private void onCancelCustomer() {
        editingId = -1;
        addCustomerForm.setVisible(false);
        addCustomerForm.setManaged(false);
        firstNameField.clear(); lastNameField.clear(); emailField.clear(); phoneField.clear();
    }

    @FXML
    private void onSaveCustomer() {
        if (firstNameField.getText().trim().isEmpty()) {
            showWarning("Please enter a first name.");
            return;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showWarning("Please enter a last name.");
            return;
        }
        customerRepository.saveCustomer(new CustomerDataRepository.CustomerInput(
                editingId >= 0 ? editingId : null,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim()
        ));
        onCancelCustomer();
        loadCustomers();
    }

    @FXML
    private void handleExportCustomers(ActionEvent event) {
        CustomerDataRepository.CustomerViewData customerData = customerRepository.loadCustomers();

        String[] headers = {"Customer Code", "First Name", "Last Name", "Email", "Phone"};

        List<String[]> data = customerData.customers().stream().map(c -> new String[]{
                safeText(c.customerCode()),
                safeText(c.firstName()),
                safeText(c.lastName()),
                safeText(c.email()),
                safeText(c.phone())
        }).collect(Collectors.toList());

        Window window = ((Node) event.getSource()).getScene().getWindow();
        ExportUtils.exportToCSV(window, "Customer_Contact_List.csv", headers, data);
    }

    private void loadCustomers() {
        allRows.clear();
        CustomerDataRepository.CustomerViewData customerData = customerRepository.loadCustomers();
        for (CustomerDataRepository.CustomerRow customer : customerData.customers()) {
            HBox row = buildRow(customer);
            String searchable = String.join(" ",
                    customer.customerCode(),
                    safeText(customer.firstName()),
                    safeText(customer.lastName()),
                    safeText(customer.email()),
                    safeText(customer.phone())
            ).toLowerCase();
            allRows.add(new RowData(searchable, row));
        }
        if (totalCustomersLabel != null) totalCustomersLabel.setText(String.valueOf(customerData.totalCustomers()));
        applyFilter();
    }

    private HBox buildRow(CustomerDataRepository.CustomerRow customer) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);

        double[] widths = {100, 200, 220, 140, 120};
        String[] values = {
                customer.customerCode(),
                customer.fullName(),
                customer.email(),
                customer.phone()
        };

        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add("table-cell");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }

        HBox actionBox = new HBox(10);
        actionBox.setPrefWidth(widths[4]);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            editingId = customer.id();
            firstNameField.setText(customer.firstName());
            lastNameField.setText(customer.lastName());
            emailField.setText(customer.email());
            phoneField.setText(customer.phone());
            addCustomerForm.setVisible(true);
            addCustomerForm.setManaged(true);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Customer");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete " + customer.fullName() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    customerRepository.deleteCustomer(customer.id());
                    loadCustomers();
                }
            });
        });

        actionBox.getChildren().addAll(editBtn, deleteBtn);
        row.getChildren().add(actionBox);

        return row;
    }

    private void applyFilter() {
        customerRows.getChildren().clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (RowData rowData : allRows) {
            if (query.isEmpty() || rowData.searchableText.contains(query)) {
                customerRows.getChildren().add(rowData.row);
            }
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Missing Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class RowData {
        private final String searchableText;
        private final HBox row;

        private RowData(String searchableText, HBox row) {
            this.searchableText = searchableText;
            this.row = row;
        }
    }
}
