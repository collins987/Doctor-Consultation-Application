# System Testing Guide - Doctor Consultation App

**Test Date**: March 19, 2026  
**Tester**: QA Team  
**Build Version**: Debug APK  
**Status**: Ready for Testing  

---

## Overview

This document provides comprehensive testing instructions for verifying all login fixes and important functionalities of the Doctor Consultation Application.

### What Was Fixed
- ✅ 24 login bugs across 3 user types
- ✅ Bcrypt password hashing implemented
- ✅ Race conditions eliminated
- ✅ Memory leaks fixed
- ✅ Dependencies modernized

### Testing Scope
This guide covers:
1. **Patient Login** - Registration, login, password validation
2. **Doctor Login** - Registration, approval workflow, login
3. **Admin Login** - Admin credentials, access control
4. **Password Hashing** - Bcrypt migration verification
5. **UI/UX Tests** - Progress dialogs, error messages, navigation
6. **Edge Cases** - Null values, empty fields, duplicate logins
7. **Performance** - Login speed, memory usage, battery impact
8. **Security** - Password encryption, session management

---

## Pre-Testing Setup

### 1. Prerequisites

**Hardware/Software Required**:
- Android Studio (latest version)
- Android Emulator API 21+ (or physical Android device)
- Firebase project configured with test data
- 2-3 test user accounts in database

**Test Data Setup**:
```
Patient Accounts:
├─ john.patient@test.com / password123
├─ jane.doe@test.com / secure456
└─ test.patient@example.com / testing789

Doctor Accounts:
├─ dr.smith@hospital.com / DocPass100 (Status: approve)
├─ dr.johnson@hospital.com / DocPass200 (Status: pending)
└─ dr.williams@hospital.com / DocPass300 (Status: reject)

Admin Accounts:
├─ admin / adminpass123
└─ superadmin / superadminpass456
```

### 2. Build and Deploy

**Generate APK**:
```bash
cd c:\Users\T37757827\Desktop\Doctor-Consultation-Application
./gradlew.bat clean assembleDebug
# APK location: app\build\outputs\apk\debug\app-debug.apk
```

**Deploy to Emulator**:
```bash
./gradlew.bat installDebug
# OR use Android Studio: Run → Run 'app'
```

**Verify Installation**:
- App icon appears on home screen
- App launches without crash
- No permission warnings

---

## Test Cases

### MODULE 1: Patient Login

#### Test 1.1: Successful Patient Login
```
Test ID: PT-LOGIN-001
Title: Valid patient login with correct credentials
Preconditions:
  - Patient account exists: john.patient@test.com / password123
  - Network connectivity available
  - App installed and running

Steps:
  1. Click "Patient Login" button on home screen
  2. Enter email: john.patient@test.com
  3. Enter password: password123
  4. Observe progress dialog appears
  5. Click "Login" button
  6. Wait for response (1-2 seconds)

Expected Results:
  ✓ Progress dialog shows "Logging in..."
  ✓ No error messages displayed
  ✓ User redirected to Patient Home Activity
  ✓ Session saved to SharedPreferences
  ✓ No back button to return to login

Postconditions:
  - User ID stored in SharedPreferences
  - Password migrated to bcrypt (if was plain-text)
  - Logcat shows: "Login successful"

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.2: Invalid Email Login
```
Test ID: PT-LOGIN-002
Title: Login with non-existent email
Preconditions:
  - App on Patient Login screen

Steps:
  1. Enter email: nonexistent@test.com
  2. Enter password: password123
  3. Click "Login" button

Expected Results:
  ✓ Show toast: "Invalid email or password"
  ✓ Remain on login screen
  ✓ Form fields cleared
  ✓ Login button re-enabled

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.3: Invalid Password Login
```
Test ID: PT-LOGIN-003
Title: Login with correct email, wrong password
Preconditions:
  - Patient john.patient@test.com exists
  - App on Patient Login screen

Steps:
  1. Enter email: john.patient@test.com
  2. Enter password: wrongpassword
  3. Click "Login" button
  4. Wait for response

Expected Results:
  ✓ Show toast: "Invalid password"
  ✓ Remain on login screen
  ✓ Login button re-enabled
  ✓ No personal data leaked in error message

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.4: Empty Fields Validation
```
Test ID: PT-LOGIN-004
Title: Validation with empty email and password
Preconditions:
  - App on Patient Login screen

Steps:
  1. Leave email field empty
  2. Leave password field empty
  3. Click "Login" button
  4. Observe error

Expected Results:
  ✓ EditText shows red error: "Email is required"
  ✓ Keyboard appears under email field
  ✓ Server not contacted (offline validation)

Then:
  1. Enter email: john.patient@test.com
  2. Leave password empty
  3. Click "Login" button

Expected Results:
  ✓ EditText shows red error: "Password is required"
  ✓ Focus moves to password field

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.5: Email Case Insensitivity (FIX #5)
```
Test ID: PT-LOGIN-005
Title: Case-insensitive email matching
Preconditions:
  - Patient: john.patient@test.com / password123

Steps:
  1. Login with: JOHN.PATIENT@TEST.COM
  2. Password: password123
  3. Click "Login" button

Expected Results:
  ✓ Login succeeds (case ignored)
  ✓ User redirected to home
  ✓ Error: NOT shown

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.6: Input Trimming (FIX #6)
```
Test ID: PT-LOGIN-006
Title: Whitespace trimmed from inputs
Preconditions:
  - Patient: john.patient@test.com / password123

Steps:
  1. Enter email: "  john.patient@test.com  " (with spaces)
  2. Enter password: "  password123  " (with spaces)
  3. Click "Login" button

Expected Results:
  ✓ Login succeeds
  ✓ Spaces automatically trimmed
  ✓ User redirected to home

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 1.7: Keyboard Dismissal (FIX #8)
```
Test ID: PT-LOGIN-007
Title: Keyboard dismisses after successful login
Preconditions:
  - Patient: john.patient@test.com / password123
  - Keyboard visible

Steps:
  1. Enter credentials
  2. Click "Login" button
  3. Observe keyboard behavior
  4. Wait for redirect

Expected Results:
  ✓ Keyboard visible while typing
  ✓ Keyboard dismisses after login click
  ✓ No keyboard visible on home screen

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 2: Doctor Login

#### Test 2.1: Approved Doctor Login Success
```
Test ID: DR-LOGIN-001
Title: Approved doctor can login
Preconditions:
  - Doctor: dr.smith@hospital.com / DocPass100
  - Status in database: "approve"

Steps:
  1. Click "Doctor Login" button
  2. Enter email: dr.smith@hospital.com
  3. Enter password: DocPass100
  4. Click "Login" button

Expected Results:
  ✓ Progress dialog appears
  ✓ Login succeeds
  ✓ Redirected to Doctor Home Activity
  ✓ Doctor details loaded (category, fees, experience)

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 2.2: Pending Doctor Rejection (FIX #7, #9)
```
Test ID: DR-LOGIN-002
Title: Pending doctor cannot login
Preconditions:
  - Doctor: dr.johnson@hospital.com / DocPass200
  - Status in database: "pending"

Steps:
  1. Click "Doctor Login" button
  2. Enter email: dr.johnson@hospital.com
  3. Enter password: DocPass200
  4. Click "Login" button

Expected Results:
  ✓ Progress dialog appears
  ✓ Login fails with message:
    "Your account is awaiting admin approval"
  ✓ Remain on login screen
  ✓ Login button re-enabled

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 2.3: Rejected Doctor Rejection
```
Test ID: DR-LOGIN-003
Title: Rejected doctor cannot login
Preconditions:
  - Doctor: dr.williams@hospital.com / DocPass300
  - Status in database: "reject"

Steps:
  1. Click "Doctor Login" button
  2. Enter email: dr.williams@hospital.com
  3. Enter password: DocPass300
  4. Click "Login" button

Expected Results:
  ✓ Progress dialog appears
  ✓ Login fails with message:
    "Your account has been rejected"
  ✓ Remain on login screen

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 2.4: Doctor Email Case Insensitivity (FIX #5)
```
Test ID: DR-LOGIN-004
Title: Doctor login is case-insensitive
Preconditions:
  - Doctor: dr.smith@hospital.com / DocPass100

Steps:
  1. Enter email: DR.SMITH@HOSPITAL.COM (uppercase)
  2. Enter password: DocPass100
  3. Click "Login" button

Expected Results:
  ✓ Login succeeds
  ✓ Email case ignored

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 3: Admin Login

#### Test 3.1: Successful Admin Login
```
Test ID: AD-LOGIN-001
Title: Valid admin login
Preconditions:
  - Admin: admin / adminpass123

Steps:
  1. Click "Admin Login" button  
  2. Enter username: admin
  3. Enter password: adminpass123
  4. Click "Login" button

Expected Results:
  ✓ Progress dialog appears
  ✓ Login succeeds
  ✓ Redirected to Admin Home Activity
  ✓ Admin dashboard visible

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 3.2: Invalid Admin Password
```
Test ID: AD-LOGIN-002
Title: Admin login with wrong password
Preconditions:
  - Admin: admin / adminpass123

Steps:
  1. Enter username: admin
  2. Enter password: wrongpass
  3. Click "Login" button

Expected Results:
  ✓ Show message: "Invalid password"
  ✓ Remain on login screen

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 4: Bcrypt Password Hashing

#### Test 4.1: Password Format in Database
```
Test ID: BC-HASH-001
Title: Verify passwords are bcrypt hashed
Preconditions:
  - Patient john.patient@test.com logged in
  - Firebase access available

Steps:
  1. Log in as patient (john.patient@test.com / password123)
  2. Open Firebase Console
  3. Navigate to Realtime Database → PatientDetails
  4. Find the logged-in user record
  5. Check the "Password" field value

Expected Results (BEFORE first login):
  ✓ Password: "password123" (plain-text)

Expected Results (AFTER first login):
  ✓ Password: "$2a$12$..." (60 characters)
  ✓ Format: bcrypt hash
  ✓ Starts with: $2a$ or $2b$

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 4.2: Bcrypt Migration Log
```
Test ID: BC-HASH-002
Title: Check bcrypt migration in logs
Preconditions:
  - Android Studio Logcat open
  - Filter: "PatientLogin" OR "AdminLogin"

Steps:
  1. Log in as patient
  2. Check Logcat for migration message
  3. Look for: "Password migrated to bcrypt"

Expected Results:
  ✓ Logcat shows migration message
  ✓ Message appears during successful login
  ✓ Only appears for first login (plain→bcrypt)
  ✓ Second login: no migration message

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 4.3: Bcrypt Verification Success
```
Test ID: BC-HASH-003
Title: Bcrypt password verification works
Preconditions:
  - Patient already logged in once (password migrated)
  - Firebase shows bcrypt hash

Steps:
  1. Log in again with same account
  2. Enter correct password
  3. Click "Login" button

Expected Results:
  ✓ Login succeeds even with bcrypt hash
  ✓ No additional migration needed
  ✓ Logcat shows: no "migrated" message
  ✓ Fast verification (~250ms)

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 5: UI/UX Tests

#### Test 5.1: Progress Dialog Display (FIX #10)
```
Test ID: UX-PROGRESS-001
Title: Progress dialog shows during login
Preconditions:
  - Patient login screen

Steps:
  1. Enter valid credentials
  2. Click "Login" button
  3. Observe dialog during network request

Expected Results:
  ✓ Dialog appears immediately
  ✓ Shows text: "Logging in..."
  ✓ Spinner/progress animation visible
  ✓ User cannot interact with other buttons
  ✓ Dialog dismisses after response (success/fail)

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 5.2: Back Button Prevention (FIX #11)
```
Test ID: UX-BACK-001
Title: Cannot go back after successful login
Preconditions:
  - Successfully logged in to Patient Home

Steps:
  1. Manually press hardware back button
  2. OR click software back button (if visible)
  3. Observe behavior

Expected Results:
  ✓ Back button/press has NO effect
  ✓ Remain on Patient Home Activity
  ✓ Do NOT return to login screen
  ✓ App uses FLAG_ACTIVITY_CLEAR_TASK

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 5.3: Error Message Display
```
Test ID: UX-ERROR-001
Title: Error messages display correctly
Preconditions:
  - Patient login screen

Steps:
  1. Enter invalid credentials
  2. Click "Login" button
  3. Observe error message

Expected Results:
  ✓ Toast appears with error message
  ✓ Message is clear and user-friendly
  ✓ Message does NOT leak sensitive info
  ✓ Message disappears after 3-4 seconds

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 6: Edge Cases & Error Handling

#### Test 6.1: Null Password in Database (FIX #4)
```
Test ID: EDGE-NULL-001
Title: Handle null password in database
Preconditions:
  - Corrupted record with null password field
  - App running

Steps:
  1. Attempt to login with affected account email
  2. Click "Login" button

Expected Results:
  ✓ No crash/NullPointerException
  ✓ Error message: "Invalid email or password"
  ✓ Logcat warning: "Incomplete user record"
  ✓ App remains stable

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 6.2: Rapid Consecutive Logins (FIX #2, #3)
```
Test ID: EDGE-RAPID-001
Title: Multiple rapid login attempts
Preconditions:
  - Patient login screen

Steps:
  1. Enter valid credentials
  2. Click "Login" button
  3. Immediately click "Login" button again (before response)
  4. Observe behavior

Expected Results:
  ✓ Button disabled after first click
  ✓ Second click has no effect
  ✓ Only one login request sent
  ✓ No duplicate logins
  ✓ Error: "Login already in progress"

Status: [PASS] [FAIL]
Notes: _________________________________________
```

#### Test 6.3: Network Timeout Handling
```
Test ID: EDGE-NETWORK-001
Title: Handle network timeout gracefully
Preconditions:
  - Slow/poor network connection

Steps:
  1. Enter valid credentials
  2. Click "Login" button
  3. Wait > 30 seconds for timeout
  4. Observe error

Expected Results:
  ✓ Progress dialog visible during wait
  ✓ After timeout: error message displayed
  ✓ Button re-enabled
  ✓ No app crash
  ✓ Can retry login

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

### MODULE 7: Performance Tests

#### Test 7.1: Login Speed
```
Test ID: PERF-LOGIN-001
Title: Login completes in acceptable time
Preconditions:
  - Good network connection
  - Patient account ready

Steps:
  1. Note start time
  2. Enter credentials
  3. Click "Login" button
  4. Note time when redirected
  5. Calculate elapsed time

Expected Results:
  ✓ Login completes in 1-2 seconds
  ✓ Bcrypt verification: ~250ms
  ✓ Network + UI: ~750ms
  ✓ Total acceptable: < 3 seconds

Status: [PASS] [FAIL]
Time Measured: _______ seconds
Notes: _________________________________________
```

#### Test 7.2: Memory Leak Check (FIX #2)
```
Test ID: PERF-MEMORY-001
Title: No memory leak on repeated logins
Preconditions:
  - Android Studio Memory Profiler open
  - Patient account ready

Steps:
  1. Note initial memory usage
  2. Log in 5 times (login → home → logout → repeat)
  3. Watch memory increase/decrease
  4. Check for plateau (no leak)

Expected Results:
  ✓ Memory increases each login
  ✓ Memory released after logout
  ✓ Memory returns to baseline
  ✓ No gradual increase (no leak)
  ✓ Old listeners properly detached

Status: [PASS] [FAIL]
Initial Memory: _______ MB
Final Memory: _______ MB
Notes: _________________________________________
```

---

### MODULE 8: Data Persistence

#### Test 8.1: Session Persistence
```
Test ID: DATA-SESSION-001
Title: Session data persisted in SharedPreferences
Preconditions:
  - Patient logged in: john.patient@test.com

Steps:
  1. Log in successfully
  2. Navigate to home screen
  3. Open device file manager/adb:
     adb shell
     cat /data/data/com.example.doctorconsultantapp/shared_prefs/Patient.xml

Expected Results:
  ✓ SharedPreferences file exists
  ✓ Contains key: "UserName"
  ✓ Contains key: "Patient_Key"
  ✓ Values match logged-in account

Status: [PASS] [FAIL]
Notes: _________________________________________
```

---

## Summary Report Template

### Test Results Summary

```
PATIENT LOGIN TESTS
  ✓ Successful login: PASS / FAIL
  ✓ Invalid email: PASS / FAIL
  ✓ Invalid password: PASS / FAIL
  ✓ Empty fields: PASS / FAIL
  ✓ Email case-insensitivity: PASS / FAIL
  ✓ Input trimming: PASS / FAIL
  ✓ Keyboard dismissal: PASS / FAIL
  
DOCTOR LOGIN TESTS
  ✓ Approved doctor: PASS / FAIL
  ✓ Pending doctor: PASS / FAIL
  ✓ Rejected doctor: PASS / FAIL
  ✓ Email case-insensitivity: PASS / FAIL
  
ADMIN LOGIN TESTS
  ✓ Valid credentials: PASS / FAIL
  ✓ Invalid password: PASS / FAIL
  
BCRYPT TESTS
  ✓ Password format (bcrypt): PASS / FAIL
  ✓ Migration log: PASS / FAIL
  ✓ Verification works: PASS / FAIL
  
UI/UX TESTS
  ✓ Progress dialog: PASS / FAIL
  ✓ Back button prevention: PASS / FAIL
  ✓ Error messages: PASS / FAIL
  
EDGE CASE TESTS
  ✓ Null handling: PASS / FAIL
  ✓ Rapid clicks: PASS / FAIL
  ✓ Network timeout: PASS / FAIL
  
PERFORMANCE TESTS
  ✓ Login speed: PASS / FAIL (_____ ms)
  ✓ Memory leak: PASS / FAIL
  
DATA PERSISTENCE TESTS
  ✓ Session saved: PASS / FAIL

TOTAL: ___ / ___ PASSED
SUCCESS RATE: ____%
```

### Issues Found

```
If any test fails, document here:

Issue 1:
  Test Case: _______________________
  Expected: _______________________
  Actual: _______________________
  Severity: Critical / High / Medium / Low
  Workaround: _______________________

Issue 2:
  (repeat above)
```

### Sign-Off

```
Tester Name: _______________________
Date: _______________________
Testing Duration: _______________________
Overall Status: PASS / FAIL / CONDITIONAL
Approval: _______________________ (signature)
```

---

## Troubleshooting During Testing

### App Won't Start
```
Solution 1: Clear app cache
  Settings → Apps → [App] → Clear Cache
  
Solution 2: Reinstall app
  ./gradlew.bat uninstallDebug
  ./gradlew.bat installDebug
  
Solution 3: Check logcat for crash
  adb logcat | grep AndroidRuntime
```

### Login Always Fails
```
Solution 1: Check Firebase connection
  - Ensure internet on device
  - Verify Firebase project configured
  - Check google-services.json exists
  
Solution 2: Check test data
  - Verify account exists in Firebase
  - Verify password is correct
  - Use Firebase Console to reset if needed
  
Solution 3: Check PasswordHasher
  - Verify bcrypt dependency installed
  - Check imports in login activities
  - Look for PasswordHasher errors in logcat
```

### Logcat Shows Errors
```
Filter for specific issues:
  adb logcat | grep "PatientLogin"
  adb logcat | grep "PasswordHasher"
  adb logcat | grep "E/"  (errors only)
  adb logcat | grep "Exception"
```

---

## References

- Complete Implementation: IMPLEMENTATION_COMPLETE.md
- Code Changes: LOGIN_BEFORE_AFTER_FIXES.md
- Bugs Fixed: LOGIN_BUG_QUICK_REFERENCE.md
- Migration Guide: DATABASE_MIGRATION_BCRYPT.md

---

**End of Testing Guide**

For additional help, see the documentation in the project root directory.
