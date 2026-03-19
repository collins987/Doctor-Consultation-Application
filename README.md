# Doctor Consultation App

A comprehensive Android-based healthcare platform enabling patients to book appointments with doctors, manage consultations, receive prescriptions, and provide feedback. Doctors can manage availability, view appointments, upload galleries, and manage prescriptions. Administrators oversee doctor approvals and system management.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Tech Stack](#tech-stack)
- [Installation & Setup](#installation--setup)
- [Usage Instructions](#usage-instructions)
- [API Overview](#api-overview)
- [Configuration](#configuration)
- [User Roles](#user-roles)
- [Security Considerations](#security-considerations)
- [Folder Structure](#folder-structure)
- [Key Components](#key-components)
- [Troubleshooting](#troubleshooting)

---

## 🏥 Project Overview

The **Doctor Consultation App** is a full-featured native Android application that bridges the gap between patients seeking medical advice and healthcare practitioners. The platform facilitates seamless appointment booking, consultation scheduling, prescription management, and quality feedback mechanisms within a secure ecosystem.

### Core Objectives:
- Enable patients to discover and book appointments with qualified doctors
- Provide doctors with tools to manage schedules, profiles, and patient interactions
- Empower administrators to maintain system integrity through doctor verification
- Integrate secure payment processing for appointment transactions
- Maintain comprehensive medical records and prescription history

---

## ✨ Key Features

### Patient Features
- **User Registration & Authentication**
  - Email-based signup with secure credential storage
  - Password recovery via email verification
  - Change password functionality
  
- **Doctor Discovery**
  - Browse doctors by medical specialty categories
  - View comprehensive doctor profiles including experience, fees, ratings
  - Search and filter by category and availability
  
- **Appointment Management**
  - Real-time appointment slot availability checking
  - Interactive date and time selection
  - Automatic filtering of booked and leave dates
  - Secure payment processing via Razorpay
  
- **Consultation History**
  - View all booked appointments (upcoming and past)
  - Access prescription documents from consultations
  - Track appointment status and doctor feedback
  
- **Feedback & Ratings**
  - Submit star ratings and comments for visited doctors
  - View collective doctor ratings and patient reviews
  
- **Communication**
  - Direct WhatsApp integration for quick contact with doctors

### Doctor Features
- **Account Management**
  - Registration with pending admin approval
  - Profile creation with specialization, experience, fees
  - Profile editing and updates
  - Password reset and security
  
- **Schedule Management**
  - Define available consultation hours per day of week
  - Add recurring time slots for different days
  - Set unavailable dates (leave/vacation)
  - Real-time slot booking synchronization
  
- **Appointment Oversight**
  - View all patient appointments
  - Check appointment status and patient details
  - Manage appointment interactions
  
- **Medical Documentation**
  - Upload prescriptions to patient records
  - Manage professional gallery images
  - View prescription history
  
- **Practice Management**
  - Monitor patient feedback and ratings
  - Track consultation history

### Admin Features
- **System Access**
  - Secure admin login with credentials
  - Change password functionality
  
- **Doctor Management**
  - Review pending doctor registrations
  - Approve verified medical professionals
  - Reject applications with incomplete credentials
  - View doctor approval status
  
- **System Oversight**
  - Monitor all registered doctors and patients
  - Manage system users and permissions

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────┐
│          ANDROID CLIENT LAYER                       │
│  (Patient/Doctor/Admin Activities & Fragments)      │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│     BUSINESS LOGIC & DATA MODELS LAYER              │
│  (Controllers, Adapters, POJO Models)               │
└─────────────────┬───────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌──────────────┐
│  FIREBASE     │   │  RAZORPAY    │
│  REALTIME     │   │  PAYMENT     │
│  DATABASE     │   │  GATEWAY     │
│               │   │              │
│ • Users       │   │ • Transaction│
│ • Slots       │   │  Processing  │
│ • Booking     │   │ • Verification
│ • Reviews     │   │              │
└───────┬───────┘   └──────────────┘
        │
        ▼
┌───────────────────────────────────┐
│    FIREBASE CLOUD STORAGE         │
│  (Doctor images, Gallery, Profiles│
└───────────────────────────────────┘
```

### Architectural Pattern: **Client-server with Cloud Backend**

- **UI Layer**: Android Activities and Fragments
- **Business Logic Layer**: Service adapters, data models, Firebase queries
- **Data Layer**: Firebase Realtime Database + Cloud Storage
- **Payment Layer**: Razorpay REST API

---

## 🛠️ Tech Stack

### Frontend
- **Language**: Java
- **Framework**: Android SDK (Min API 19, Target API 30, Compile API 33)
- **UI Components**: AndroidX, Material Design 1.8.0
- **Navigation**: Android Navigation Component 2.5.1
- **Image Library**: Picasso, CircleImageView

### Backend & Data
- **Primary Database**: Firebase Realtime Database
- **Cloud Storage**: Firebase Cloud Storage
- **Authentication Fallback**: Firebase Authentication (available but not actively used)
- **Database Collections**: DoctorDetails, PatientDetails, BookingDetails, BookedSlots, DoctorSlots, RatingDetails

### Networking & Integration
- **HTTP Client**: Android Volley 1.2.1
- **JSON Serialization**: Gson 2.8.9
- **Payment Gateway**: Razorpay Checkout 1.5.5
- **Communication**: WhatsApp Business API

### Build & Dependencies
- **Build System**: Gradle 7.2.2 (AGP 7.2.2)
- **Google Services Plugin**: 4.3.13
- **Additional Libraries**:
  - Sliding Root Navigation 1.1.0
  - MultiDex Support
  - AndroidX AppCompat 1.6.0-beta01

---

## 📦 Installation & Setup

### Prerequisites
- Android Studio Bumblebee or later
- JDK 11+
- Gradle 7.2.2+
- Android Device or Emulator (API 19+)
- Firebase Project Account
- Razorpay Merchant Account (Test/Live Keys)

### Step 1: Clone & Open Project
```bash
git clone <repository-url>
cd Doctor-Consultation-Application
open with Android Studio
```

### Step 2: Firebase Configuration
1. Create a Firebase project at [firebase.google.com](https://firebase.google.com)
2. Download `google-services.json` from Firebase Console
3. Place it in: `app/` directory
4. Enable these Firebase services:
   - Realtime Database
   - Cloud Storage
   - Authentication (optional, currently unused)

### Step 3: Configure Razorpay
1. Obtain Razorpay API keys from [razorpay.com](https://razorpay.com)
2. In `AndroidManifest.xml`, update:
   ```xml
   <meta-data
       android:name="io.razorpay.api_key"
       android:value="rzp_test_xxxxxxxxxxxx" />
   ```
3. For production: Replace test key with live key

### Step 4: Build the Project
```bash
# Build debug APK
./gradlew build

# Build release APK (requires keystore)
./gradlew bundleRelease
```

### Step 5: Run on Device/Emulator
```bash
# Install debug app
./gradlew installDebug

# Launch app
adb shell am start -n com.example.doctorconsultantapp/.SplashScreen

# OR use Android Studio: Run > Run 'app'
```

### Firebase Realtime Database Initial Structure
After first app launch, the following nodes will be auto-created:
```
Firebase Root
├── DoctorDetails
├── PatientDetails
├── BookingDetails
├── BookedSlots
├── DoctorSlots
├── DoctorGallery
├── LeaveDetail
├── RatingDetails
└── Admin
```

---

## 🚀 Usage Instructions

### For Patients

#### Sign Up
1. Select "Patient" from role selection screen
2. Click "Sign Up"
3. Enter: Full Name, Email, Password, Phone Number
4. Tap "Register"
5. Account created immediately (no approval needed)

#### Book Appointment
1. Navigate to Home
2. Browse doctors by category OR search
3. Select a doctor to view profile
4. Choose "Book Appointment"
5. Select date and available time slot
6. Review appointment details
7. Proceed to payment via Razorpay
8. Upon successful payment, appointment is confirmed
9. Check "My Appointments" for booking status

#### View Prescriptions
1. Go to "My Appointments"
2. Select a past consultation
3. View and download prescription document

#### Leave Review
1. Navigate to doctor profile
2. Click "Write Review"
3. Rate doctor (1-5 stars)
4. Add comments
5. Submit review

### For Doctors

#### Register & Get Approved
1. Select "Doctor" from role selection screen
2. Fill registration form:
   - Full Name, Email, Password
   - Medical Category (Cardiologist, Dentist, etc.)
   - Phone Number, Fee per consultation
   - Experience (years)
   - Services offered, Address
   - Upload profile photo
3. Submit registration
4. Status: "pending" (await admin approval)
5. Admin reviews and approves/rejects
6. Upon approval, status becomes "approve"

#### Set Availability
1. Navigate to "Manage Schedule"
2. Select day of week
3. Enter start and end consultation hours
4. Add multiple slots as needed
5. Slots immediately reflect in appointment booking

#### Mark Leave/Vacation
1. Go to "Leave & Schedule"
2. Select date range for unavailability
3. Add reason (optional)
4. Patients cannot book on marked leave dates

#### Upload Prescriptions
1. Navigate to "Manage Prescriptions"
2. Select "Upload Prescription"
3. Enter prescription title and details
4. Attach prescription document/image
5. Save - prescription linked to patient consultation

#### Manage Gallery
1. Go to "Gallery"
2. Tab: "Add Gallery"
3. Choose image from camera or device
4. Add caption
5. Upload to Firebase Storage
6. Gallery visible in doctor profile

#### View Reviews
1. Check "My Profile" → "Patient Reviews"
2. View all ratings and comments
3. Track rating trend

### For Admin

#### Login
1. Select "Admin" from role selection screen
2. Enter Username and Password
3. Access admin dashboard

#### Approve Doctors
1. Navigate to "Manage Doctors"
2. Tab: "Pending Approvals" (status: "pending")
3. Review doctor profile and credentials
4. Click "Approve" to set status to "approve"
5. Doctor account now active

#### Reject Doctors
1. Navigate to "Manage Doctors"
2. Tab: "Rejections" (status: "reject")
3. Review doctor details
4. Click "Reject" if credentials insufficient
5. Doctor cannot access app

#### System Management
1. Change admin password: Settings → Change Password
2. Monitor all doctors and patients
3. Maintain system integrity

---

## 🔌 API Overview

### Firebase Realtime Database Endpoints

The app communicates directly with Firebase RTDB collections without a REST backend. Key operations:

#### Doctor Operations
```
GET   /DoctorDetails         - Fetch all doctors
GET   /DoctorDetails/{d_key} - Fetch specific doctor
POST  /DoctorDetails         - Create new doctor
PUT   /DoctorDetails/{d_key} - Update doctor profile
QUERY /DoctorDetails?orderByChild=category&equalTo=Cardiologist - Filter by category
QUERY /DoctorDetails?orderByChild=status&equalTo=approve - Only approved doctors
```

#### Appointment Operations
```
GET   /BookingDetails                    - All appointments
POST  /BookingDetails                    - Create new booking
GET   /BookedSlots?orderByChild=date     - View booked slots for date
POST  /BookedSlots                       - Reserve slot
PUT   /BookingDetails/{bookingKey}       - Update appointment status
```

#### Doctor Schedule
```
GET   /DoctorSlots/{doctorId}                           - All doctor slots
POST  /DoctorSlots/{doctorId}                           - Add new slot
GET   /DoctorSlots/{doctorId}?orderByChild=day          - Slots for specific day
```

#### Patient Operations
```
GET   /PatientDetails/{patientKey}  - Fetch patient profile
POST  /PatientDetails               - Create patient account
PUT   /PatientDetails/{patientKey}  - Update patient info
```

#### Review Operations
```
GET   /RatingDetails?orderByChild=d_key&equalTo={doctorId} - All reviews for doctor
POST  /RatingDetails                                        - Submit new review
```

### External API Integration

#### Razorpay Payment Gateway
```
POST https://api.razorpay.com/v1/orders    - Create payment order
Event: onPaymentSuccess(orderId)           - Payment verification
Event: onPaymentError(code, error)         - Payment failure handling
```

#### WhatsApp Integration
```
Intent: https://api.whatsapp.com/send?phone={number}&text={message}
Usage: Direct messaging from doctor profile
```

---

## ⚙️ Configuration

### Environment Variables & Keys

#### Firebase Configuration (`app/google-services.json`)
```json
{
  "project_info": {
    "project_number": "778552137947",
    "project_id": "doctorconsultionapp",
    "storage_bucket": "doctorconsultionapp.appspot.com",
    "api_key": "AIzaSyBxP65Lm2l4SLD6gnroID6czn-FF0izYGE"
  }
}
```

#### Razorpay Keys (`AndroidManifest.xml`)
```xml
<!-- TEST MODE (for development) -->
<meta-data
    android:name="io.razorpay.api_key"
    android:value="rzp_test_96HeaVmgRvbrfT" />

<!-- For production, replace with live key from Razorpay dashboard -->
```

#### SharedPreferences Storage Locations
- **Doctor Session**: `SharedPreferences("Doctor").getString("Doctorid")`
- **Patient Session**: `SharedPreferences("Patient").getString("PatientId")`
- **Admin Session**: Stored in LoginActivity

#### Firebase Realtime Database Rules (Recommended Security)
```json
{
  "rules": {
    "DoctorDetails": {
      ".read": "auth != null",
      ".write": "root.child('Admin').child($uid).exists()"
    },
    "PatientDetails": {
      "$uid": {
        ".read": "auth.uid == $uid || root.child('Admin').child(auth.uid).exists()",
        ".write": "auth.uid == $uid"
      }
    },
    "BookingDetails": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

### Local Development Configuration
```properties
# gradle.properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m
```

---

## 👥 User Roles

### Patient Role
- **Permissions**: Browse doctors, book appointments, view prescriptions, rate doctors, manage profile
- **Authentication**: Self-registration (no approval)
- **Access Level**: Full patient portal access upon signup
- **Data Isolation**: Can only view own appointments and prescriptions

### Doctor Role
- **Permissions**: Manage schedule, upload prescriptions, manage gallery, edit profile
- **Authentication**: Registration with pending status
- **Access Level**: Limited until admin approval (status = "approve")
- **Approval Workflow**: 
  1. Doctor registers → status = "pending"
  2. Admin reviews → status = "approve" or "reject"
  3. Once approved → Full doctor portal access
- **Data Isolation**: Can only manage own schedule, gallery, and prescriptions

### Admin Role
- **Permissions**: Approve/reject doctors, manage system, change password
- **Authentication**: Credentials stored in Firebase (`Admin/{username}/{password}`)
- **Access Level**: Full system administration
- **Responsibilities**: 
  - Verify doctor credentials
  - Manage system integrity
  - Monitor user activity

---

## 🔒 Security Considerations

### Current Security Implementation
✅ **What's Implemented**:
- Firebase Authentication integration available
- Cloud Storage for image hosting
- Razorpay PCI-DSS compliant payment processing
- WhatsApp end-to-end encryption for communication

### ⚠️ Security Concerns & Recommendations

#### 1. Password Management
**Issue**: Passwords stored in plain text in Firebase RTDB
**Risk**: Medium - Database compromise exposes passwords
**Recommendation**: 
```
USE Firebase Authentication service instead of custom password validation
- Implement sign-in with email/password via FirebaseAuth
- Enable firebase.auth.FirebaseUser for session management
```

#### 2. Data Encryption
**Issue**: Sensitive data (prescriptions, patient info) not encrypted at rest
**Recommendation**:
```
- Implement encryption for sensitive fields using Cipher class
- Use Android KeyStore for key management
- Encrypt prescriptions before upload to storage
```

#### 3. API Key Protection
**Issue**: Razorpay API keys visible in code/manifest
**Recommendation**:
```
- Store keys in Razorpay Android SDK (built-in)
- Never commit keys to version control
- Use build variants for test/production keys
```

#### 4. Authorization
**Issue**: Limited role-based access control (RBAC)
**Recommendation**:
```
- Implement Firebase Security Rules with custom claims
- Add role verification on each sensitive operation
- Implement token-based authorization
```

#### 5. Data Privacy (Healthcare Compliance)
**Issue**: Medical data not HIPAA/privacy regulation compliant
**Recommendation**:
```
- Implement data anonymization for analytics
- Add audit logs for all data access
- Enable Firebase backup/recovery procedures
- Comply with regional healthcare data protection laws
```

### Secure Development Practices
1. **Never log sensitive data** (passwords, payment tokens)
2. **Use HTTPS only** for all network communication
3. **Implement certificate pinning** for Firebase connections
4. **Validate all user inputs** before Firebase queries
5. **Regularly audit Firebase Security Rules**
6. **Keep dependencies updated** to patch vulnerabilities

---

## 📂 Folder Structure

```
Doctor-Consultation-Application/
│
├── app/                                 # Main app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/doctorconsultantapp/
│   │   │   │   ├── SplashScreen.java              # App launcher
│   │   │   │   ├── MainActivity.java              # Role selection
│   │   │   │   │
│   │   │   │   ├── [Patient Activities]/
│   │   │   │   │   ├── PatientLoginActivity.java
│   │   │   │   │   ├── PatientSignupActivity.java
│   │   │   │   │   ├── PatientHomeActivity.java
│   │   │   │   │   ├── BookAppointmentActivity.java
│   │   │   │   │   ├── ConfirmAppointmentActivity.java (Razorpay)
│   │   │   │   │   ├── ViewAllDoctorsActivity.java
│   │   │   │   │   └── ReviewDoctorActivity.java
│   │   │   │   │
│   │   │   │   ├── [Doctor Activities]/
│   │   │   │   │   ├── DoctorLoginActivity.java
│   │   │   │   │   ├── DoctorSignupActivity.java
│   │   │   │   │   ├── DoctorHomeActivity.java
│   │   │   │   │   ├── AddScheduleActivity.java
│   │   │   │   │   ├── ManagePrescriptionActivity.java
│   │   │   │   │   └── ManageGalleryActivity.java
│   │   │   │   │
│   │   │   │   ├── [Admin Activities]/
│   │   │   │   │   ├── AdminHome.java
│   │   │   │   │   └── ManageDoctorActivity.java
│   │   │   │   │
│   │   │   │   ├── [Data Models]/
│   │   │   │   │   ├── Doctor_details.java
│   │   │   │   │   ├── PatientDetails.java
│   │   │   │   │   ├── Booking.java
│   │   │   │   │   ├── PrescriptionDetails.java
│   │   │   │   │   ├── ReviewDetails.java
│   │   │   │   │   └── CategoryDetails.java
│   │   │   │   │
│   │   │   │   ├── [Fragments - Tabbed UI]/
│   │   │   │   │   ├── AddFrag.java              # Upload gallery
│   │   │   │   │   ├── ViewFrag.java             # View gallery
│   │   │   │   │   ├── UploadFrag.java           # Upload prescription
│   │   │   │   │   ├── ViewAllFrag.java          # View prescriptions
│   │   │   │   │   ├── Approvefrag.java          # Pending doctors
│   │   │   │   │   └── PostReview.java           # Submit review
│   │   │   │   │
│   │   │   │   └── [Utilities]/
│   │   │   │       ├── Doctor_Global_Class.java  # Session storage
│   │   │   │       └── Adapters/                 # RecyclerView adapters
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/                       # Activity/Fragment layouts (54 files)
│   │   │   │   │   └── activity_*.xml
│   │   │   │   │
│   │   │   │   ├── drawable/                     # Vector drawables & shapes (30+ files)
│   │   │   │   │   ├── button_style_*.xml
│   │   │   │   │   ├── ic_*.xml                  # Icons
│   │   │   │   │   └── gradient*.xml             # Background gradients
│   │   │   │   │
│   │   │   │   ├── drawable-v21/                 # API 21+ specific resources
│   │   │   │   ├── drawable-v24/                 # API 24+ specific resources
│   │   │   │   ├── values/                       # Colors, strings, styles
│   │   │   │   ├── mipmap-*/                     # App icon variants
│   │   │   │   ├── menu/                         # Navigation menus
│   │   │   │   ├── navigation/                   # Navigation graphs
│   │   │   │   └── anim/                         # Animation definitions
│   │   │   │
│   │   │   └── AndroidManifest.xml               # App configuration & permissions
│   │   │
│   │   ├── androidTest/                          # Instrumented tests
│   │   └── test/                                 # Unit tests
│   │
│   ├── build.gradle                              # App-level build config
│   ├── proguard-rules.pro                        # ProGuard obfuscation rules
│   └── google-services.json                      # Firebase configuration
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
│
├── build.gradle                                  # Root-level build config
├── settings.gradle                               # Gradle project settings
├── gradle.properties                             # Gradle system properties
├── gradlew & gradlew.bat                         # Gradle wrapper scripts
│
└── README.md                                     # This file
```

---

## 🧩 Key Components

### Authentication System
- **Patient**: Self-registration, email/password lookup in `PatientDetails`
- **Doctor**: Registration with pending approval, verification in admin dashboard
- **Admin**: Fixed credentials in `Admin/{username}` node
- **Session Management**: SharedPreferences (`Doctor`, `Patient` keys)

### Appointment Logic
- **Booking Flow**: DatePicker → Available slots → Payment → Confirmation
- **Slot Management**: Doctor defines slots by day/time, system blocks booked slots
- **Leave Handling**: Doctor blocks dates, booking system filters them out
- **Payment**: Razorpay integration on confirmation

### Real-time Synchronization
- **ValueEventListener**: All data updates reflected in real-time
- **ListenerForSingleValueEvent**: One-time reads for efficiency
- **Query Ordering**: `.orderByChild()` for filtered results

### Image Management
- **Upload**: Camera/Gallery picker → Firebase Storage (`/doctorimages/`)
- **Retrieval**: URL stored in RTDB, Picasso for async loading
- **Types**: Profile pictures, gallery images, prescriptions

### Payment Processing
- **Gateway**: Razorpay Checkout
- **Flow**: Amount calculation → Payment → Success/Error handler → RTDB update
- **Key**: Test key in manifest (replace for production)

---

## 🔧 Troubleshooting

### App Crashes on Launch
**Symptom**: SplashScreen crashes immediately
**Solution**:
```
1. Check google-services.json is in app/ directory
2. Verify Firebase project ID matches in AndroidManifest.xml
3. Ensure INTERNET permission is added in AndroidManifest.xml
4. Clear app cache: adb shell pm clear com.example.doctorconsultantapp
```

### Firebase Connection Failed
**Symptom**: "Unable to connect to database" error
**Solution**:
```
1. Verify internet connectivity on device
2. Check Firebase project is active in Firebase Console
3. Verify Realtime Database is created (not in Firestore mode)
4. Check Firebase Security Rules allow read/write
5. Confirm google-services.json is correct
```

### Razorpay Payment Error
**Symptom**: Payment button shows error or doesn't respond
**Solution**:
```
1. Verify Razorpay API key in AndroidManifest.xml is correct
2. Ensure amount > 0 before payment initiation
3. Check device has internet for payment processing
4. For test mode: Use test card details from Razorpay docs
5. Check Razorpay SDK version in build.gradle is 1.5.5+
```

### Doctor Registration Stuck on "Pending"
**Symptom**: Doctor signup complete but dashboard shows "pending" status
**Solution**:
```
1. Login as Admin
2. Navigate to Manage Doctors → Pending Approvals
3. Verify doctor credentials
4. Click Approve button to change status to "approve"
5. Doctor can now access full features
```

### Image Upload to Gallery Fails
**Symptom**: Upload button crashes or image doesn't appear
**Solution**:
```
1. Check WRITE_EXTERNAL_STORAGE permission is granted on Android 6.0+
2. Verify Firebase Storage is enabled in Firebase Console
3. Check Firebase Storage rules allow write for authenticated users
4. Ensure image size < 10 MB
5. Verify doctor is logged in (has valid d_key in database)
```

### Appointment Slots Not Showing
**Symptom**: "No slots available" even after doctor adds schedule
**Solution**:
```
1. Verify doctor has added schedule in AddScheduleActivity
2. Check availability isn't blocked by leave dates
3. Confirm booking date isn't in the past
4. Restart app to refresh real-time data
5. Check doctor's schedule hours cover desired time
```

### Password Reset Email Not Received
**Symptom**: Click "Forgot Password" but no email arrives
**Solution**:
```
1. Verify email address is registered in system
2. Check spam/junk folder for email
3. Ensure internet connectivity
4. Wait 5-10 minutes (email delivery delayed)
5. Check backend API is reachable (if using custom backend)
```

---

## 📞 Support & Contribution

### Getting Help
- Check [Troubleshooting](#troubleshooting) section above
- Review Firebase documentation: https://firebase.google.com/docs
- Check Razorpay integration guide: https://razorpay.com/docs/

### Development Guidelines
- Follow Android coding standards
- Test on multiple API levels (19, 24, 30)
- Use Firebase Emulator for local testing
- Implement proper error handling with try-catch
- Add null checks for Firebase queries

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/appointment-history

# Make changes and test
git add .
git commit -m "feat: add appointment history view"

# Push and create pull request
git push origin feature/appointment-history
```

---

## 📄 License

[License information to be determined by project repository]

---

## 🙏 Acknowledgments

- Built with Android SDK, Kotlin/Java
- Powered by Firebase platform
- Payment processing via Razorpay
- UI components from Material Design

---

**Last Updated**: March 2026  
**Version**: 1.0.0  
**Status**: Production Ready

