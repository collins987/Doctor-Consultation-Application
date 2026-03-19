# Database Migration Strategy - Plain Text to Bcrypt

**Version**: 1.0  
**Date**: March 19, 2026  
**Status**: CRITICAL - HIPAA/GDPR Compliance Required  

---

## Executive Summary

This document outlines the strategy for migrating existing plain-text passwords to bcrypt-hashed passwords in the Firebase Realtime Database.

**Current State**: All 3 user types (Patient, Doctor, Admin) have plain-text passwords stored  
**Target State**: All passwords hashed with bcrypt, compliant with HIPAA §164.312(a)(2)(i)  
**Timeline**: 2-3 weeks (phased approach)  
**Impact**: Zero downtime, users unaffected  

---

## Phase 1: Preparation (Day 1-2)

### 1.1 Code Deployment

✅ **Already Completed**:
- PasswordHasher.java utility class created
- LoginUtils.java utility class created
- All three login activities updated to support both plain-text AND bcrypt
- build.gradle updated with bcrypt dependency

### 1.2 Pre-flight Check

Run this command in Android Studio Terminal:

```bash
./gradlew clean build
```

**Expected Output**: 
```
BUILD SUCCESSFUL in Xs
```

If build fails, check:
- bcrypt dependency in build.gradle (org.mindrot:jbcrypt:0.4)
- PasswordHasher.java compilation errors
- Missing imports in login activities

### 1.3 Database Backup

**CRITICAL**: Backup your Firebase database before migration.

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Realtime Database → ⋮ (menu) → Backups
4. Click "Create Backup"
5. Name it: `pre-bcrypt-migration-YYYY-MM-DD`
6. Wait for backup to complete (usually 5-10 minutes)

---

## Phase 2: Testing (Day 3-5)

### 2.1 Local Testing

1. Deploy the updated APK to test device
2. Test all three login flows with existing accounts:

```
Test Case 1: Patient Login
├─ Email: john@test.com
├─ Password: (current plain-text password from DB)
└─ Expected: Login succeeds, password STILL plain-text in DB

Test Case 2: Doctor Login
├─ Email: doctor@test.com
├─ Password: (current plain-text password from DB)
├─ Status: approve
└─ Expected: Login succeeds, password STILL plain-text in DB

Test Case 3: Admin Login
├─ Username: admin1
├─ Password: (current plain-text password from DB)
└─ Expected: Login succeeds, password STILL plain-text in DB
```

**Why passwords stay plain-text?**  
Phase 2 is read-only. LoginActivity checks if password is bcrypt format via `isBcryptHash()`. If plain-text, it verifies against plain-text, then migrates to bcrypt. This is safe because:
- Original password is preserved during verification
- Migration only happens after successful verification
- If verification fails, no migration occurs

### 2.2 New User Registration Testing

After code deployment, any NEW registrations will automatically use bcrypt:

1. Create new patient account
2. Check Firebase: password field should be `$2a$...` (bcrypt format)
3. Test login: should work with bcrypt verification

---

## Phase 3: Automated User Migration (Day 6-14)

### 3.1 Strategy: Login-Based Migration

The safest approach: Migrate passwords one-at-a-time as users log in.

**How it works**:
1. User logs in with their existing plain-text password
2. LoginActivity receives plain-text from EditText
3. PasswordHasher.verifyPassword() is called
4. Inside PasswordHasher: `isBcryptHash()` detects plain-text format
5. If password matches plain-text, return true AND migrate:

```java
if (isMatch) {
    // Migrate to bcrypt during next login
    String hashed = PasswordHasher.hashPassword(password);
    database.getReference("Admin/" + username + "/password")
           .setValue(hashed);
}
```

6. User is logged in successfully
7. Next time they log in, their password will be hashed (bcrypt + verification)

**Advantages**:
- ✅ Zero risk: only migrates after verification
- ✅ Transparent: users don't know it's happening
- ✅ Automatic: no manual intervention needed
- ✅ Gradual: spreads load over time

**Timeline**: When will 100% be migrated?
- Day 1: Test accounts logging in
- Week 2-4: Active users logging in
- Week 4+: Dormant accounts (may never migrate)

### 3.2 Checking Migration Progress

**Firebase Console Method**:

1. Go to Realtime Database
2. Click "PatientDetails" → Select a patient
3. Look at "Password" field:
   - Plain-text: `mypass123` → NOT migrated yet
   - Bcrypt: `$2a$12$...` (60 chars) → MIGRATED ✅

**Android Logs**:

Check Logcat for migration debug messages:

```
Filter: "AdminLogin" OR "DoctorLogin" OR "PatientLogin"

Look for:
D/AdminLogin: Password migrated to bcrypt
```

### 3.3 Manual Migration Script (Optional)

If you want to migrate accounts immediately without waiting for logins:

**⚠️ WARNING**: This is more complex and requires care with Realtime Database rules.

Create a Cloud Function (Node.js) in Firebase:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const bcrypt = require('bcrypt');

// ⚠️ CAUTION: This function directly modifies passwords
// Only run if users have not yet logged in

admin.initializeApp();

exports.migratePatientPasswords = functions.https.onCall(async (data, context) => {
    // Check authentication
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be logged in');
    }
    
    const db = admin.database();
    const patientsRef = db.ref('PatientDetails');
    
    let migrated = 0;
    let failed = 0;
    
    try {
        const snapshot = await patientsRef.once('value');
        
        for (const [key, patient] of Object.entries(snapshot.val() || {})) {
            try {
                const password = patient.Password;
                
                // Skip if already hashed
                if (password.startsWith('$2a$') || password.startsWith('$2b$')) {
                    console.log(`Skipping ${key} - already migrated`);
                    continue;
                }
                
                // Hash password
                const hashedPassword = await bcrypt.hash(password, 12);
                
                // Update database
                await patientsRef.child(key).update({
                    Password: hashedPassword
                });
                
                migrated++;
                console.log(`Migrated ${key}`);
            } catch (err) {
                failed++;
                console.error(`Failed to migrate ${key}:`, err);
            }
        }
        
        return {
            success: true,
            migrated: migrated,
            failed: failed,
            total: migrated + failed
        };
    } catch (err) {
        console.error('Migration error:', err);
        throw new functions.https.HttpsError('internal', 'Migration failed: ' + err.message);
    }
});
```

**Deploy this function**:
```bash
firebase deploy --only functions:migratePatientPasswords
```

**Call from Android** (if needed):
```java
FirebaseFunctions functions = FirebaseFunctions.getInstance();
functions.getHttpsCallable("migratePatientPasswords")
    .call()
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
            int migrated = ((Number) result.get("migrated")).intValue();
            Toast.makeText(this, "Migrated: " + migrated, Toast.LENGTH_SHORT).show();
        }
    });
```

---

## Phase 4: Verification & Cleanup (Day 15+)

### 4.1 Audit Checklist

After 2-3 weeks, review:

```markdown
- [ ] All test accounts migrated to bcrypt (check Firebase)
- [ ] All active users can still log in successfully
- [ ] No support tickets about login failures
- [ ] Logcat shows successful password verifications
- [ ] Random spot-checks of 10+ accounts show bcrypt passwords
```

### 4.2 Dormant Account Migration

For accounts that haven't logged in:

**Option A**: Wait indefinitely
- Pro: Zero risk, no forced migration
- Con: Some passwords remain plain-text indefinitely

**Option B**: Force re-registration
- Pro: 100% bcrypt compliance
- Con: Extra work for dormant users

**Option C**: Scheduled Cloud Function
- Pro: Automatic, 100% compliance
- Con: Requires server-side code

**Recommendation**: Option C (scheduled function) + grace period notification

### 4.3 Remove Plain-Text Support (30-60 days later)

Once 95%+ are migrated, you can remove plain-text fallback from code:

```java
// BEFORE (current)
if (PasswordHasher.isBcryptHash(storedPassword)) {
    isMatch = PasswordHasher.verifyPassword(password, storedPassword);
} else {
    // Support plain-text fallback
    isMatch = password.equals(storedPassword);
    if (isMatch) {
        String hashed = PasswordHasher.hashPassword(password);
        database.getReference(...).setValue(hashed);
    }
}

// AFTER (clean)
isMatch = PasswordHasher.verifyPassword(password, storedPassword);
```

---

## Rollback Procedure

If critical issues arise:

### 4.4 Rollback Steps

1. **Restore from backup** (Firebase Console):
   - Production → ⋮ (menu) → Restore
   - Select pre-bcrypt backup
   - Confirm and wait for restoration

2. **Revert app code**:
   - Go back to previous APK (before bcrypt changes)
   - Redeploy to users
   - Clear app cache: `./gradlew clean`

3. **Diagnosis**: Check logs to determine issue
   - Memory leak? (Look for repeated login attempts)
   - UI hang? (Progress dialog not dismissing)
   - Password format error? (Check PasswordHasher logs)

4. **Fix and re-deploy**:
   - Fix the identified issue
   - Test thoroughly
   - Redeploy with next version

---

## Security Considerations

### 4.5 Bcrypt Configuration

Current settings in PasswordHasher.java:

```java
private static final int COST = 12;
```

**COST = 12** means:
- 2^12 = 4096 iterations
- ~250ms per hash on modern hardware (appropriate for mobile)
- Recommended for Android (not too slow, not too weak)

**DO NOT** increase to 14+ on mobile (will slow login to 1-2 seconds)

### 4.6 Compliance

✅ **HIPAA §164.312(a)(2)(i)** - Encryption and decryption of ePHI  
✅ **GDPR Article 32** - Security of processing  
✅ **Best Practice** - NIST 800-63B recommends bcrypt for password hashing

---

## Monitoring & Maintenance

### 4.7 Ongoing Monitoring

**Weekly Checks**:

```bash
# Check login success rate
firebase-cli analytics get PatientLoginActivity login_success
firebase-cli analytics get DoctorLoginActivity login_success
firebase-cli analytics get LoginActivity login_success

# Monitor for bcrypt errors in Crashlytics
Firebase Crashlytics → Filter: PasswordHasher
```

**Monthly Checks**:

1. Random spot-check of 20 accounts
2. Verify passwords are in bcrypt format
3. Test login for each verified account
4. Check for un-migrated accounts

### 4.8 Documentation

Once migration complete, update:

1. **Code comments** in LoginActivity:
   ```java
   // All passwords are now bcrypt-hashed as of 2026-04-01
   // Plain-text fallback removed in v2.0
   ```

2. **Security policy document**:
   - Add: "Passwords hashed using bcrypt (cost factor 12)"
   - Link to this migration guide

3. **Support documentation**:
   - "If login fails after password change, password was not migrated"
   - "Contact support to reset password"

---

## FAQ

### Q: What if a user forgets their plain-text password before migration?
**A**: They must use "Forgot Password" feature. The reset email sends temporary password that they can use to log in and set new password. The new password will be bcrypt-hashed.

### Q: Will login be slower with bcrypt?
**A**: Minimally. Bcrypt with COST=12 takes ~250ms, which is acceptable for mobile. Users won't notice difference.

### Q: What if someone brute-forces the database backup?
**A**: Bcrypt allows only ~1-2 attempts per second (due to high COST), making brute-force impractical. Your plain-text backup must be destroyed after migration.

### Q: Can I export users' plain-text passwords before migration?
**A**: **NO**. Never export plain-text passwords. If you need password for any reason, user must reset via "Forgot Password".

### Q: How do I test that a password is bcrypt-hashed?
**A**: Use PasswordHasher.isBcryptHash(password) method, or check Firebase Console: bcrypt hashes are always 60 characters starting with "$2a$" or "$2b$".

---

## Troubleshooting

| Symptom | Cause | Solution |
|---------|-------|----------|
| Login fails with correct password | Password not bcrypt hash, incorrect plain-text | Check Firebase: is password 60 chars starting with $2a$? If not, use password reset |
| Bcrypt verification throws exception | Corrupted password in database | Delete user record, have user re-register |
| Migration function slow | Too many users, COST factor high | Wait for scheduled runs, or reduce COST to 10 (NOT recommended) |
| Some users still plain-text after 1 month | Users haven't logged in | Send notification: "Update your app and log in" |

---

## Implementation Checklist

Create a markdown checklist in your project:

```markdown
# Bcrypt Migration Checklist

## Phase 1: Preparation
- [ ] Code deployed with bcrypt support
- [ ] build.gradle includes bcrypt dependency
- [ ] PasswordHasher.java compiles without errors
- [ ] All login activities updated
- [ ] Gradient builds successfully

## Phase 2: Testing
- [ ] Test login with existing plain-text passwords
- [ ] Test new user registration (should use bcrypt)
- [ ] Verify passwords in Firebase after login (should see $2a$)
- [ ] Test all three user types (Patient, Doctor, Admin)

## Phase 3: Rollout
- [ ] Release app update to beta testers
- [ ] Beta testers report no login issues
- [ ] Release to production
- [ ] Monitor Crashlytics for PasswordHasher errors

## Phase 4: Monitoring (Week 1-4)
- [ ] Check daily that login success rate > 98%
- [ ] Spot-check 5 accounts every Friday
- [ ] Verify passwords are bcrypt format

## Phase 5: Verification (Week 4+)
- [ ] 100% of active users migrated
- [ ] Dormant accounts handled (manual migration or notification)
- [ ] Cleanup: remove Plain-text fallback from code
- [ ] Documentation updated

## Security Sign-off
- [ ] Security team approves migration strategy
- [ ] Compliance officer signs off on HIPAA/GDPR
- [ ] Database backup created and stored safely
```

---

## Summary

| Phase | Timeline | Action |
|-------|----------|--------|
| **Preparation** | Day 1-2 | Deploy code, backup database |
| **Testing** | Day 3-5 | Test with existing accounts |
| **Migration** | Day 6-14 | Users log in, passwords auto-migrate |
| **Verification** | Week 3-4 | Confirm 95%+ migrated |
| **Cleanup** | Week 5+ | Remove plain-text fallback, handle dormant accounts |

**Total Timeline**: 3-5 weeks for full compliance
**User Impact**: None - transparent migration
**Risk Level**: Low - all migrations verified before database update

---

**End of Database Migration Guide**

For questions, see LOGIN_ANALYSIS_AND_ROOT_CAUSES.md or LOGIN_BEFORE_AFTER_FIXES.md
