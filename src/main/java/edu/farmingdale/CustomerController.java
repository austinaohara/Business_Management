package edu.farmingdale;

import edu.farmingdale.repository.CustomerDataRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CustomerController implements Refreshable {

    @FXML private VBox addCustomerForm;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField;
    @FXML private TextField searchField;
    @FXML private VBox customerRows;
    @FXML private HBox templateRow;
    @FXML private Label totalCustomersLabel;

    private final List<RowData> allRows = new ArrayList<>();
    private final CustomerDataRepository customerRepository = new CustomerDataRepository();

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
        customerRepository.saveCustomer(new CustomerDataRepository.CustomerInput(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim()
        ));
        onCancelCustomer();
        loadCustomers();
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
        double[] widths = {100, 200, 220, 140, 80};
        String[] values = {
                customer.customerCode(),
                customer.fullName(),
                customer.email(),
                customer.phone(),
                ""
        };
        for (int i = 0; i < values.length; i++) {
            Label lbl = new Label(values[i]);
            lbl.getStyleClass().add("table-cell");
            lbl.setPrefWidth(widths[i]);
            row.getChildren().add(lbl);
        }
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

    private static class RowData {
        private final String searchableText;
        private final HBox row;

        private RowData(String searchableText, HBox row) {
            this.searchableText = searchableText;
            this.row = row;
        }
    }
}
