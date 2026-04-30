package edu.farmingdale.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void getFullNameCombinesFirstAndLastName() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com", "555-1111");

        assertEquals("Ada Lovelace", customer.getFullName());
    }

    @Test
    void updateNameRejectsBlankFirstName() {
        Customer customer = new Customer("Ada", "Lovelace", null, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customer.updateName("   ", "Lovelace")
        );

        assertEquals("firstName must not be blank.", exception.getMessage());
    }
}
