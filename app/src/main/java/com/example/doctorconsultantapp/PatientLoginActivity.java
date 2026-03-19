package com.example.doctorconsultantapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * ✅ FIXED VERSION: All 9 login bugs corrected
 * See: LOGIN_ANALYSIS_AND_ROOT_CAUSES.md, LOGIN_BEFORE_AFTER_FIXES.md
 * 
 * Fixes implemented:
 * #1: Bcrypt password hashing
 * #2: addListenerForSingleValueEvent (no memory leak)
 * #3: Login logic inside callback (no race condition)
 * #4: Null safety checks
 * #5: Case-insensitive email
 * #6: EditText trim()
 * #8: Keyboard dismissal
 * #10: Progress dialog
 * #11: Back button prevention
 */
public class PatientLoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mainref;
    private ProgressDialog progressDialog;
    private boolean isLoginInProgress = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_patient_login);

        emailEditText = findViewById(R.id.staffname);
        passwordEditText = findViewById(R.id.staffpassword);
        loginButton = findViewById(R.id.Bt1);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mainref = firebaseDatabase.getReference("PatientDetails");
        
        loginButton.setOnClickListener(v -> {
            // ✅ FIX #6: Trim whitespace from inputs
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // ✅ Input validation
            if (!validateInputs(email, password)) {
                return;
            }

            loginPatient(email, password);
        });
    }

    /**
     * ✅ FIX #10: Added input validation
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!LoginUtils.isValidEmail(email)) {
            emailEditText.setError("Please enter valid email");
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (!LoginUtils.isValidPassword(password)) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * ✅ FIXED: All bugs corrected
     * Original bugs: #1, #2, #3, #4, #5, #6, #8, #10, #11
     */
    private void loginPatient(String email, String password) {
        // ✅ FIX #10: Show progress dialog
        showProgress("Logging in...");

        // ✅ FIX #2: Prevent multiple simultaneous requests
        if (isLoginInProgress) {
            showError("Login already in progress. Please wait...");
            hideProgress();
            return;
        }
        isLoginInProgress = true;
        loginButton.setEnabled(false);

        // ✅ FIX #2: Use addListenerForSingleValueEvent (NOT addValueEventListener)
        mainref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hideProgress();

                // ✅ FIX #3: Logic INSIDE callback (not before)

                if (!dataSnapshot.exists()) {
                    showError("User not found");
                    resetLoginUI();
                    return;
                }

                // ✅ FIX #4: Safe loop with null checks
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        PatientDetails patient = snapshot.getValue(PatientDetails.class);

                        // ✅ FIX #4: Guard clauses for null
                        if (patient == null || patient.getEmail() == null || patient.getPassword() == null) {
                            Log.w("PatientLogin", "Incomplete user record: " + snapshot.getKey());
                            continue;
                        }

                        // ✅ FIX #5: Case-insensitive email comparison
                        if (patient.getEmail().equalsIgnoreCase(email)) {
                            // ✅ FIX #1: Use bcrypt for password verification (NOT plain text!)
                            if (PasswordHasher.verifyPassword(password, patient.getPassword())) {
                                // Password matched!
                                String patientKey = snapshot.getKey();

                                // Save session
                                SharedPreferences sharedPreferences = getSharedPreferences("Patient", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("UserName", patient.getEmail());
                                editor.putString("Patient_Key", patientKey);
                                editor.commit();

                                // ✅ FIX #8: Dismiss keyboard
                                dismissKeyboard();

                                // ✅ FIX #3: Navigate ONLY after successful verification
                                navigateToHome();
                                return;
                            } else {
                                // Password mismatch - continue loop in case of duplicates
                                showError("Invalid password");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PatientLogin", "Error processing login", e);
                        showError("Login error. Please try again.");
                    }
                }

                // No matching user found
                showError("Invalid email or password");
                resetLoginUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgress();
                showError("Database error: " + databaseError.getMessage());
                resetLoginUI();
            }
        });
    }

    /**
     * ✅ FIX #8: Dismiss keyboard after login
     */
    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    /**
     * ✅ FIX #10: Show progress dialog
     */
    private void showProgress(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * ✅ FIX #10: Hide progress dialog
     */
    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Reset UI after failed login attempt
     */
    private void resetLoginUI() {
        isLoginInProgress = false;
        loginButton.setEnabled(true);
    }

    /**
     * ✅ FIX #11: Prevent back navigation after login
     */
    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), PatientHomeActivity.class);
        // ✅ FIX #11: Clear back stack with these flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String error) {
        Toast.makeText(PatientLoginActivity.this, error, Toast.LENGTH_SHORT).show();
    }

    public void go(View view) {
        Intent intent = new Intent(this, PatientSignupActivity.class);
        startActivity(intent);
    }

    public void go2(View view) {
        Intent intent = new Intent(this, PatientForgetPasswordActivity.class);
        startActivity(intent);
    }
}



