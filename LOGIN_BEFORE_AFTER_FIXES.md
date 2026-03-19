# LOGIN ACTIVITIES - BEFORE & AFTER CODE FIXES

## FILE 1: PatientLoginActivity.java

### Issue 1: Bug #2 + #3 (Listener detachment & Race condition) - Lines 52-87

**BEFORE (BROKEN)**:
```java
mainref.addValueEventListener(new ValueEventListener() {  // ❌ Bug #2: Never detached
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot Admin : dataSnapshot.getChildren()) {
            PatientDetails obj = Admin.getValue(PatientDetails.class);  // ❌ Bug #4: No null check
            if (username.equals(obj.Email) && pass.equals(obj.getPassword())) {  // ❌ Bug #5: Case sensitive
                SharedPreferences sharedPreferences = getSharedPreferences("Patient", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", obj.getEmail());
                editor.putString("Patient_Key", obj.getPatientKey());
                editor.commit();
                flag = 1;
                break;
            }
        }
        if (flag == 1) {  // ✓ Correct placement - inside callback
            Intent intent = new Intent(getApplicationContext(), PatientHomeActivity.class);
            startActivity(intent);
            Toast.makeText(PatientLoginActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PatientLoginActivity.this, "Login unsuccessfull", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) { }
});
```

**AFTER (FIXED)**:
```java
mainref.addListenerForSingleValueEvent(new ValueEventListener() {  // ✅ Fix #2: Single event only
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            PatientDetails obj = snap.getValue(PatientDetails.class);
            // ✅ Fix #4: Null checks
            if (obj == null || obj.getEmail() == null || obj.getPassword() == null) {
                continue;
            }
            // ✅ Fix #5: Case-insensitive email, trim values
            // ✅ Fix #1: Will use BCrypt after implementing password hashing
            if (username.equalsIgnoreCase(obj.getEmail()) && pass.equals(obj.getPassword())) {
                SharedPreferences sharedPreferences = getSharedPreferences("Patient", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", obj.getEmail());
                editor.putString("Patient_Key", obj.getPatientKey());
                editor.commit();
                flag = 1;
                break;
            }
        }
        // ✅ Fix #3: Flag check remains in callback (correct)
        if (flag == 1) {
            Intent intent = new Intent(getApplicationContext(), PatientHomeActivity.class);
            // ✅ Fix #11: Add flags to prevent back navigation
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(PatientLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PatientLoginActivity.this, "Login unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(PatientLoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

### Issue 2: Bug #6 (Untrimmed values) - Lines 48-49

**BEFORE (BROKEN)**:
```java
final String username = et1.getText().toString();  // ❌ Bug #6: Not trimmed
final String pass = et2.getText().toString();
```

**AFTER (FIXED)**:
```java
final String username = et1.getText().toString().trim().toLowerCase();  // ✅ Trim & lowercase for case-insensitive
final String pass = et2.getText().toString().trim();  // ✅ Trim but keep case for password
```

### Issue 3: Bug #8 (Keyboard) - In onClick method

**BEFORE (BROKEN)**:
```java
Bt1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        final String username = et1.getText().toString().trim().toLowerCase();
        final String pass = et2.getText().toString().trim();
        if (et1.getText().toString().trim().length() == 0) {
            // ... validation ...
        } else {
            // Query Firebase - keyboard still visible
```

**AFTER (FIXED)**:
```java
Bt1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // ✅ Fix #8: Hide keyboard immediately
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        
        // ✅ Fix #10 (Optional): Show progress
        ProgressDialog progressDialog = new ProgressDialog(PatientLoginActivity.this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        Bt1.setEnabled(false);
        
        final String username = et1.getText().toString().trim().toLowerCase();
        final String pass = et2.getText().toString().trim();
        if (et1.getText().toString().trim().length() == 0) {
            // ... validation ...
        } else {
            mainref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // ... login logic ...
                    progressDialog.dismiss();  // ✅ Hide progress
                    Bt1.setEnabled(true);
                }
            });
```

---

## FILE 2: DoctorLoginActivity.java

### Issue 1: Bug #4, #5, #7 (Null checks, case-sensitive, status) - Lines 64-95

**BEFORE (BROKEN)**:
```java
mainref1.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Log.d("mymsg", dataSnapshot.toString());
        if (dataSnapshot.exists()) {
            for (DataSnapshot Doctor : dataSnapshot.getChildren()) {
                Doctor_details obj = Doctor.getValue(Doctor_details.class);  // ❌ Bug #4: No null check
                // ❌ Bug #7: Could crash here if obj, getEmail(), getPassword() null
                Log.d("mymsg", Doctorid + "..." + obj.getEmail() + "...." + pass + "..." + obj.getPassword());
                // ❌ Bug #5: Case sensitive email comparison
                if (Doctorid.equals(obj.getEmail()) && pass.equals(obj.getPassword())) {
                    // ❌ Bug #7: obj.getStatus() could be null
                    if (obj.getStatus().equals("approve")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("Doctor", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Doctorid", obj.getD_key());
                        editor.putString("DoctorEmail", obj.getEmail());
                        editor.commit();
                        flag = 1;
                        break;
                    } else {
                        flag = 4;
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(DoctorLoginActivity.this, "DB Not Exists", Toast.LENGTH_SHORT).show();
        }
        if (flag == 1) {
            Intent intent = new Intent(getApplicationContext(), DoctorHomeActivity.class);
            startActivity(intent);
            Toast.makeText(DoctorLoginActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
        } else if (flag == 4) {
            Toast.makeText(DoctorLoginActivity.this, "Your Status is Pending plz contact to admin", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DoctorLoginActivity.this, "Login unsuccessfull", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) { }
});
```

**AFTER (FIXED)**:
```java
mainref1.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Log.d("mymsg", dataSnapshot.toString());
        if (dataSnapshot.exists()) {
            for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                Doctor_details obj = doctorSnapshot.getValue(Doctor_details.class);
                
                // ✅ Fix #4 & #7: Comprehensive null checks
                if (obj == null || obj.getEmail() == null || obj.getPassword() == null || obj.getStatus() == null) {
                    continue;  // Skip malformed records
                }
                
                // ✅ Fix #5: Case-insensitive email comparison
                // ✅ Fix #1: Will use BCrypt after implementing password hashing
                if (Doctorid.equalsIgnoreCase(obj.getEmail()) && pass.equals(obj.getPassword())) {
                    if (obj.getStatus().equals("approve")) {  // ✅ Safe - already checked null
                        SharedPreferences sharedPreferences = getSharedPreferences("Doctor", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Doctorid", obj.getD_key());
                        editor.putString("DoctorEmail", obj.getEmail());
                        editor.commit();
                        flag = 1;
                        break;
                    } else {
                        flag = 4;
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(DoctorLoginActivity.this, "Database not found", Toast.LENGTH_SHORT).show();
        }
        
        if (flag == 1) {
            Intent intent = new Intent(getApplicationContext(), DoctorHomeActivity.class);
            // ✅ Fix #11: Add flags to prevent back navigation
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(DoctorLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        } else if (flag == 4) {
            Toast.makeText(DoctorLoginActivity.this, "Your account is pending approval. Please contact admin.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DoctorLoginActivity.this, "Login unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(DoctorLoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

### Issue 2: Bug #6 (Trim values) - Lines 48-49

**BEFORE (BROKEN)**:
```java
final String Doctorid = et1.getText().toString();
final String pass = et2.getText().toString();
```

**AFTER (FIXED)**:
```java
final String Doctorid = et1.getText().toString().trim().toLowerCase();  // ✅ Trim & lowercase
final String pass = et2.getText().toString().trim();  // ✅ Trim
```

---

## FILE 3: LoginActivity.java (Admin)

### Issue 1: Bug #4, #5 (Null checks, case-sensitive) - Lines 52-60

**BEFORE (BROKEN)**:
```java
for (DataSnapshot Admin : dataSnapshot.getChildren()) {
    Login obj = Admin.getValue(Login.class);  // ❌ Bug #4: No null check
    if (username.equals(obj.UserName) && pass.equals(obj.getPassword())) {  // ❌ Bug #5: Case sensitive
        SharedPreferences sharedPreferences = getSharedPreferences("Admin", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserName", obj.getUserName());
        editor.apply();
        flag = 1;
        break;
    }
}
```

**AFTER (FIXED)**:
```java
for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
    Login obj = adminSnapshot.getValue(Login.class);
    
    // ✅ Fix #4: Null check
    if (obj == null || obj.getUserName() == null || obj.getPassword() == null) {
        continue;
    }
    
    // ✅ Fix #5: Case-insensitive comparison
    // ✅ Fix #1: Will use BCrypt after implementing password hashing
    if (username.equalsIgnoreCase(obj.getUserName()) && pass.equals(obj.getPassword())) {
        SharedPreferences sharedPreferences = getSharedPreferences("Admin", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserName", obj.getUserName());
        editor.apply();
        flag = 1;
        break;
    }
}
```

### Issue 2: Bug #6 (Trim values) - Lines 48-49

**BEFORE (BROKEN)**:
```java
final String username = et1.getText().toString();
final String pass = et2.getText().toString();
```

**AFTER (FIXED)**:
```java
final String username = et1.getText().toString().trim().toLowerCase();  // ✅ Trim & lowercase
final String pass = et2.getText().toString().trim();  // ✅ Trim
```

### Issue 3: Bug #11 (Back button prevention) - Line 61

**BEFORE (BROKEN)**:
```java
Intent intent = new Intent(getApplicationContext(), AdminHome.class);
startActivity(intent);  // ❌ No flags - user can press back
```

**AFTER (FIXED)**:
```java
Intent intent = new Intent(getApplicationContext(), AdminHome.class);
// ✅ Fix #11: Add flags to prevent back navigation
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
```

---

## CRITICAL FIX: IMPLEMENT PASSWORD HASHING

### Add to build.gradle
```gradle
dependencies {
    // ... existing dependencies ...
    implementation 'org.mindrot:jbcrypt:0.4'
}
```

### In ALL Signup Activities:
```java
import org.mindrot.bc.BCrypt;

// When saving to Firebase during signup:
String rawPassword = passwordEditText.getText().toString();
String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
patientDetails.setPassword(hashedPassword);  // Store hash, not plain text
databaseRef.setValue(patientDetails);
```

### In ALL Login Activities (Replace plain comparison):
```java
// ❌ OLD (Plain text):
if (pass.equals(obj.getPassword())) {

// ✅ NEW (Hash comparison):
if (BCrypt.checkpw(pass, obj.getPassword())) {
```

---

## PRIORITY IMPLEMENTATION ORDER

1. **Implement password hashing** (Bug #1) - modify signup → modify all 3 login activities
2. **Add null checks** (Bug #4) - PatientLoginActivity, DoctorLoginActivity, LoginActivity
3. **Fix PatientLoginActivity listener** (Bug #2) - change `addValueEventListener` to `addListenerForSingleValueEvent`
4. **Add case-insensitive email** (Bug #5) - all 3 activities
5. **Trim EditText values** (Bug #6) - all 3 activities
6. **Add keyboard dismissal** (Bug #8) - all 3 activities
7. **Add back button prevention** (Bug #11) - all 3 activities
8. **Optional: Add progress dialog** (Bug #10) - all 3 activities

