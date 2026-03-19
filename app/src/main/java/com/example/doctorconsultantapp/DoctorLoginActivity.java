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
 * ✅ FIXED VERSION: All 7 login bugs corrected
 * See: LOGIN_ANALYSIS_AND_ROOT_CAUSES.md, LOGIN_BEFORE_AFTER_FIXES.md
 * 
 * Fixes implemented:
 * #1: Bcrypt password hashing
 * #4: Null safety checks
 * #5: Case-insensitive email
 * #6: EditText trim()
 * #7: Consistent status validation
 * #8: Keyboard dismissal
 * #9: Status checking improvements
 * #10: Progress dialog
 * #11: Back button prevention
 */
public class DoctorLoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mainref1;
    private ProgressDialog progressDialog;
    private boolean isLoginInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_doctor_login);
        emailEditText = findViewById(R.id.staffname);
        passwordEditText = findViewById(R.id.staffpassword);
        loginButton = findViewById(R.id.Bt1);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mainref1 = firebaseDatabase.getReference("DoctorDetails");

        loginButton.setOnClickListener(view -> {
            // ✅ FIX #6: Trim whitespace from inputs
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            
            if (!validateInputs(email, password)) {
                return;
            }
            
            loginDoctor(email, password);
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

        return true;
    }

    /**
     * ✅ FIXED: All bugs corrected
     * Original bugs: #1, #4, #5, #6, #7, #8, #9, #10, #11
     */
    private void loginDoctor(String email, String password) {
        showProgress("Logging in...");  // ✅ FIX #10

        if (isLoginInProgress) {
            showError("Login in progress...");
            hideProgress();
            return;
        }
        isLoginInProgress = true;
        loginButton.setEnabled(false);

        mainref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hideProgress();  // ✅ FIX #10

                Log.d("DoctorLogin", "Data snapshot: " + dataSnapshot.toString());
                
                if (!dataSnapshot.exists()) {
                    showError("Doctor not found");
                    resetLoginUI();
                    return;
                }

                for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Doctor_details doctor = doctorSnapshot.getValue(Doctor_details.class);
                        
                        if (doctor == null) {
                            Log.w("DoctorLogin", "Doctor object is null");
                            continue;
                        }

                        String dbEmail = doctor.getEmail();
                        String hashedPassword = doctor.getPassword();
                        String status = doctor.getStatus();

                        // ✅ FIX #4 & #7: Null checks
                        if (dbEmail == null || hashedPassword == null) {
                            Log.w("DoctorLogin", "Incomplete doctor record: " + doctorSnapshot.getKey());
                            continue;
                        }

                        Log.d("DoctorLogin", "Comparing: " + email + " with " + dbEmail);

                        // ✅ FIX #5: Case-insensitive email comparison
                        if (dbEmail.equalsIgnoreCase(email)) {
                            // ✅ FIX #1: Bcrypt verification
                            if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                                // ✅ FIX #9: Consistent status checking using LoginUtils
                                if (!LoginUtils.isDoctorApproved(status)) {
                                    if (LoginUtils.isPending(status)) {
                                        showError("Your account is awaiting admin approval");
                                    } else {
                                        showError("Your account has been rejected");
                                    }
                                    resetLoginUI();
                                    return;
                                }

                                // Doctor approved - proceed with login
                                String doctorKey = doctorSnapshot.getKey();

                                // Save session
                                SharedPreferences sharedPreferences = getSharedPreferences("Doctor", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("Doctorid", doctorKey);
                                editor.putString("DoctorEmail", dbEmail);
                                editor.commit();

                                // Also save to global class (existing pattern)
                                Doctor_Global_Class.category = doctor.getCategory();
                                Doctor_Global_Class.dname = doctor.getFullName();
                                Doctor_Global_Class.dphone = doctor.getPhoneNo();

                                // ✅ FIX #8: Dismiss keyboard
                                dismissKeyboard();
                                
                                navigateToHome();
                                return;
                            } else {
                                showError("Invalid password");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DoctorLogin", "Error processing login", e);
                    }
                }

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
     * ✅ FIX #8: Dismiss keyboard
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
        Intent intent = new Intent(getApplicationContext(), DoctorHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    public void go(View view) {
        Intent intent = new Intent(this, DoctorSignupActivity.class);
        startActivity(intent);
    }

    public void go2(View view) {
        Intent intent = new Intent(getApplicationContext(), Doctor_Forget_Password.class);
        startActivity(intent);
    }
}



