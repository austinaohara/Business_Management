package edu.farmingdale.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashProducesRecognizedFormatThatMatchesOriginalPassword() {
        String password = "MyS3cret!";

        String hash = PasswordHasher.hash(password);

        assertNotEquals(password, hash);
        assertTrue(PasswordHasher.isHashFormat(hash));
        assertTrue(PasswordHasher.matches(password, hash));
    }

    @Test
    void matchesRejectsWrongPassword() {
        String hash = PasswordHasher.hash("CorrectHorse");

        assertFalse(PasswordHasher.matches("WrongBattery", hash));
    }

    @Test
    void hashRejectsBlankPasswords() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordHasher.hash("  ")
        );

        assertEquals("password must not be blank.", exception.getMessage());
    }
}
