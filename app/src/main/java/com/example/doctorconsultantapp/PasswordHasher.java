package com.example.doctorconsultantapp;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Secure password hashing using bcrypt algorithm.
 * HIPAA/GDPR compliant password storage.
 * See: LOGIN_BEFORE_AFTER_FIXES.md for usage examples
 */
public class PasswordHasher {
    
    /** Bcrypt cost factor (iterations). Higher = slower but more secure. */
    private static final int COST = 12;
    
    /**
     * Hash a plain text password using bcrypt.
     * This should be done when storing passwords (registration or password change).
     *
     * @param plainPassword The password entered by user
     * @return Hashed password in bcrypt format (starts with $2a$)
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Generate salt and hash in one call
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }
    
    /**
     * Verify a plain text password against a bcrypt hash.
     * This is done during login to check if entered password matches stored hash.
     *
     * @param plainPassword Password entered by user
     * @param hashedPassword Hashed password stored in database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            // Return false for null inputs instead of crashing
            if (plainPassword == null || hashedPassword == null) {
                return false;
            }
            
            // This is the secure comparison - takes provided time regardless of match
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid bcrypt hash format
            // This can happen with old plain text passwords or corrupted data
            return false;
        }
    }
    
    /**
     * Check if a hash is in bcrypt format (started as bcrypt hash, not migrated from plain text).
     * Useful for one-time migration of old passwords.
     *
     * @param hash The value from database
     * @return true if hash is bcrypt format, false if plain text
     */
    public static boolean isBcryptHash(String hash) {
        if (hash == null) return false;
        
        // Bcrypt hashes always start with $2a$, $2b$, or $2x$
        return hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}");
    }
}
