package com.example.doctorconsultantapp;

/**
 * Centralized constants and utilities for login/authentication.
 * Ensures consistency across all three login types (Patient, Doctor, Admin).
 * See: LOGIN_BEFORE_AFTER_FIXES.md for usage examples
 */
public class LoginUtils {
    
    // Doctor account status constants
    public static final String STATUS_PENDING = "pending";      // Pending admin approval
    public static final String STATUS_APPROVED = "approve";     // Admin approved (existing)
    public static final String STATUS_REJECTED = "reject";      // Admin rejected
    
    /**
     * Check if doctor account is approved for login.
     */
    public static boolean isDoctorApproved(String status) {
        return STATUS_APPROVED.equals(status);
    }
    
    /**
     * Check if doctor still pending approval.
     */
    public static boolean isPending(String status) {
        return STATUS_PENDING.equals(status);
    }
    
    /**
     * Check if doctor was rejected.
     */
    public static boolean isRejected(String status) {
        return STATUS_REJECTED.equals(status);
    }
    
    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return email != null 
            && !email.isEmpty() 
            && email.contains("@") 
            && email.contains(".");
    }
    
    /**
     * Validate password (minimum 6 characters).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
