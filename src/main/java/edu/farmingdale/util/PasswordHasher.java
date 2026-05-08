package edu.farmingdale.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public final class PasswordHasher {
    private static final String HASH_PREFIX = "pbkdf2";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank.");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password.toCharArray(), salt, ITERATIONS);

        return HASH_PREFIX
                + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null || !isHashFormat(storedHash)) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
        byte[] actualHash = deriveKey(password.toCharArray(), salt, iterations);
        return Arrays.equals(expectedHash, actualHash);
    }

    public static boolean isHashFormat(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String[] parts = value.split("\\$");
        if (parts.length != 4 || !HASH_PREFIX.equals(parts[0])) {
            return false;
        }

        try {
            Integer.parseInt(parts[1]);
            Base64.getDecoder().decode(parts[2]);
            Base64.getDecoder().decode(parts[3]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to hash password.", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }
}
