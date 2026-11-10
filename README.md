Azure swagger api : https://stocksync-bwdtd2b6hpc9dec8.uaenorth-01.azurewebsites.net/swagger/index.html

# StockSync — Mobile Stock Management Application

**Final POE Submission — v1.0.0**

A native Android stock and sales management application built in Kotlin with Jetpack Compose, backed by a custom ASP.NET Core 8 REST API, PostgreSQL database, and Firebase Firestore (NoSQL).

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [POE Advanced Features](#poe-advanced-features)
4. [Project Structure](#project-structure)
5. [Full Setup Instructions](#full-setup-instructions)
6. [Generating a Signed Release APK](#generating-a-signed-release-apk)
7. [Firebase Setup](#firebase-setup)
8. [Version Control and CI/CD](#version-control-and-cicd)
9. [Demonstration Video](#demonstration-video)
10. [Release Notes](#release-notes)
11. [AI Usage Summary](#ai-usage-summary)
12. [Play Store Publication](#play-store-publication)
13. [Technology Stack](#technology-stack)

---

## Overview

StockSync is designed for small and informal business owners in South Africa. It provides offline-first stock tracking, quick-sale logging, low-stock alerts, a daily sales dashboard, barcode scanning, biometric authentication, store location tracking, multi-language support (English and isiZulu), and NoSQL cloud data sync via Firebase Firestore.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│              ANDROID DEVICE (Kotlin)                      │
│                                                           │
│  Jetpack Compose UI → ViewModel (MVVM)                    │
│       → Repository                                        │
│           → RoomDB (SQLite) + BLOB storage                │
│           → Firestore (NoSQL)                             │
│           → Retrofit/OkHttp → HTTPS → REST API           │
│                                                           │
│  WorkManager Sync │ FCM │ ML Kit │ DataStore              │
│  EncryptedSharedPreferences │ BiometricPrompt             │
│  CameraX │ Google Play Services Location                  │
└────────────────────────┬──────────────────────────────────┘
                         │ HTTPS
┌────────────────────────▼──────────────────────────────────┐
│           ASP.NET Core 8 Web API (C#)                     │
│                                                           │
│  Controllers → Services → Repositories                    │
│       → EF Core → PostgreSQL                             │
│                                                           │
│  JWT Auth │ FCM Push Notifications                        │
└───────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│              Firebase Firestore (NoSQL)                   │
│                                                           │
│  Collections: sales, products                             │
│  Document-based queries with filtering and ordering       │
└───────────────────────────────────────────────────────────┘
```

### Data Flow

1. **Local writes** go to RoomDB (SQLite) with BLOB storage for photos.
2. **Pending changes** are queued in `pending_sync` table for offline sync.
3. **Sales and products** are also written to Firestore (NoSQL) for cloud analytics.
4. **WorkManager** runs periodic background sync when network is available.
5. **REST API** receives batched sync data and persists to PostgreSQL.

---

## POE Advanced Features

| Requirement | Implementation | Files |
|-------------|---------------|-------|
| Biometric Authentication | AndroidX BiometricPrompt with PIN fallback | `BiometricScreen.kt` |
| Offline Mode + Sync | RoomDB + WorkManager + pending sync queue | `StockSyncDatabase.kt`, `SyncWorker.kt`, `StockSyncRepository.kt` |
| Real-time Notifications | Firebase Cloud Messaging | `StockSyncMessagingService.kt` |
| Multi-Language Support | English + isiZulu with runtime switching | `values/strings.xml`, `values-zu/strings.xml` |
| BLOB Storage | Product photos stored as byte arrays in RoomDB | `Entities.kt` (ByteArrayConverter), `AddProductScreen.kt` |
| NoSQL Store + Read | Firebase Firestore write and read operations | `FirestoreManager.kt` |
| Production Assets | Custom adaptive icon, signed release build | `build.gradle.kts`, `ic_launcher.xml` |
| CI/CD | GitHub Actions builds + tests on every push | `.github/workflows/android-ci.yml` |

---

## Project Structure

```
.
├── StockSync/                    # Android app (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts      # App build config + signing + dependencies
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── assets/
│   │       │   └── demo-video-script.md
│   │       ├── java/com/stocksync/app/
│   │       │   ├── data/
│   │       │   │   ├── api/           # Retrofit API client + DTOs
│   │       │   │   ├── db/            # RoomDB entities, DAOs, database, BLOB converter
│   │       │   │   ├── firestore/     # FirestoreManager (NoSQL operations)
│   │       │   │   ├── preferences/   # DataStore + EncryptedSharedPreferences
│   │       │   │   └── repository/    # StockSyncRepository
│   │       │   ├── ui/
│   │       │   │   ├── screens/       # All Compose screens + ViewModels
│   │       │   │   └── navigation/    # Navigation graph
│   │       │   ├── sync/             # WorkManager + FCM service
│   │       │   ├── util/             # ValidationUtils
│   │       │   ├── StockSyncApp.kt
│   │       │   └── MainActivity.kt
│   │       └── res/
│   │           ├── values/            # English strings.xml
│   │           └── values-zu/         # isiZulu strings.xml
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
│
├── StockSyncApi/                 # ASP.NET Core 8 Web API
│   ├── StockSyncApi.sln
│   └── StockSyncApi/
│       ├── Controllers/          # Auth, Products, Sales, Sync, Dashboard, etc.
│       ├── Services/             # AuthService, ProductService
│       ├── Models/               # User, Settings, Product, Sale, StockAdjustment
│       ├── DTOs/                 # Request/Response DTOs
│       └── Data/                 # StockSyncDbContext (EF Core)
│
├── .github/workflows/
│   └── android-ci.yml           # GitHub Actions CI workflow
│
├── RELEASE_NOTES.md             # Formal release notes
├── ai-usage-writeup.html        # AI usage summary
└── README.md                    # This file
```

---

## Full Setup Instructions

### Prerequisites

- **Android Studio** (Hedgehog 2023.1.1 or newer)
- **JDK 17** (bundled with Android Studio)
- **Android SDK** API level 34 (Android 14)
- **An Android phone** with USB debugging enabled (required for POE — no emulator)
- **Visual Studio 2022** or **VS Code** with .NET 8 SDK (for the API)
- **PostgreSQL** (local or cloud-hosted)
- **Git** and a **GitHub** account
- **Google Cloud Console** account (for Firebase + SSO)
- A **USB cable** to connect your phone

---

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/stocksync.git
cd stocksync
```

### Step 2: Firebase Setup

You must create a Firebase project and download the config file. This is required for FCM push notifications and Firestore NoSQL database.

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click **Add Project** and name it "StockSync"
3. Add an **Android app** with package name `com.stocksync.app`
4. Download the `google-services.json` file
5. Place it at `StockSync/app/google-services.json`
6. In Firebase Console, go to **Firestore Database** → **Create database** → Start in test mode
7. In Google Cloud Console, create an OAuth 2.0 Web application client ID. Use this same client ID as `googleServerClientId` in `StockSync/gradle.properties` and as `GOOGLE_CLIENT_ID` in the deployed API environment.

### Step 3: Add the Google Services Gradle Plugin

Add the plugin to the project-level `build.gradle.kts`:

```kotlin
// StockSync/build.gradle.kts — add to top-level plugins block
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

Add to `StockSync/app/build.gradle.kts` plugins block:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}
```

### Step 4: API Setup

1. Open `StockSyncApi/StockSyncApi.sln` in Visual Studio or VS Code
2. Configure the database connection in `appsettings.json`:
   ```json
   "ConnectionStrings": {
     "DefaultConnection": "Host=localhost;Database=stocksync;Username=postgres;Password=YOUR_PASSWORD"
   }
   ```
3. For local development without PostgreSQL, the API uses an in-memory database automatically
4. Run the API:
   ```bash
   cd StockSyncApi/StockSyncApi
   dotnet run
   ```
5. The API will be available at `http://localhost:5000` with Swagger UI at `http://localhost:5000/swagger`

### Step 5: Configure the Android App for Production

Deploy the API to a public HTTPS host with PostgreSQL before recording the final demonstration. The Android build reads these values from `StockSync/gradle.properties`:

```properties
stocksyncApiUrl=https://YOUR_HOSTED_API_URL/
googleServerClientId=YOUR_GOOGLE_WEB_CLIENT_ID
```

Set the deployed API environment variables `GOOGLE_CLIENT_ID` and `JWT_KEY`, and configure the production PostgreSQL connection. For local emulator testing, keep `stocksyncApiUrl=http://10.0.2.2:5000/`. Do not use the local value in the final physical-device recording.

### Step 6: Open the Project in Android Studio

1. Open Android Studio
2. Select **Open** and choose the `StockSync` folder
3. Wait for Gradle sync to complete (this may take a few minutes the first time)
4. If prompted, install the missing SDK versions (API 34)

### Step 7: Enable USB Debugging on Your Phone

1. On your phone, go to **Settings → About phone**
2. Tap **Build number** 7 times to enable Developer Options
3. Go to **Settings → Developer Options**
4. Enable **USB Debugging**
5. Connect your phone to your computer with a USB cable
6. Accept the RSA key prompt on your phone

### Step 8: Run the App

1. In Android Studio, select your phone from the device dropdown (top toolbar)
2. Click the green **Run** button (or press Shift+F10)
3. The app will build, install, and launch on your phone

### Step 9: Run Unit Tests

```bash
cd StockSync
./gradlew testDebugUnitTest
```

API tests:
```bash
cd StockSyncApi
dotnet test
```

---

## Generating a Signed Release APK

### 1. Generate a Keystore

```bash
keytool -genkey -v -keystore stocksync-release.keystore -alias stocksync -keyalg RSA -keysize 2048 -validity 10000
```

Enter a password and fill in the certificate details when prompted.

### 2. Create keystore.properties

Create a file at `StockSync/keystore.properties` (do NOT commit this to git):

```properties
storeFile=../stocksync-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=stocksync
keyPassword=YOUR_KEY_PASSWORD
```

### 3. Build the Signed APK

```bash
cd StockSync
./gradlew assembleRelease
```

The signed APK will be at:
```
StockSync/app/build/outputs/apk/release/app-release.apk
```

### 4. Add keystore.properties to .gitignore

Create or edit `StockSync/.gitignore` and add:
```
keystore.properties
*.keystore
*.jks
```

---

## Version Control and CI/CD

### Git Setup

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/stocksync.git
git push -u origin main
```

### Final POE Tag

After your final commit:
```bash
git tag -a "Final POE" -m "Final POE submission"
git push origin "Final POE"
```

### CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/android-ci.yml`) runs on every push:
- Runs Android lint for static analysis
- Builds the Android debug APK
- Runs Android unit tests
- Builds the ASP.NET Core API
- Runs API unit tests + integration tests with code coverage
- Publishes the API as a deployment artifact
- Deploys the API to Azure staging (when Azure secrets are configured)
- Builds a signed release APK (when signing secrets are configured)
- Uploads APK, lint results, test results, and coverage as artifacts

---

## Demonstration Video

**[INSERT YOUR UNLISTED YOUTUBE VIDEO LINK HERE]**

The video must demonstrate (on a physical phone):
1. Authentication via SSO + Biometric verification
2. Modifying and saving user preferences in Settings
3. Real-time data communication with the REST API
4. Offline mode: disconnect network, perform action, reconnect, show sync
5. Cloud/DB: data changes visible in API and database
6. Core features: dashboard, products, quick sale, barcode scanning, store location

See `StockSync/app/src/main/assets/demo-video-script.md` for the full script.

---

## Release Notes

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the complete list of features added since the prototype phase.

---

## Play Store Publication

Full store listing details (title, description, keywords, content rating, screenshots list) and step-by-step Play Console upload instructions are in [STORE_LISTING.md](STORE_LISTING.md).

A privacy policy template is provided at [privacy-policy.html](privacy-policy.html) — host it on GitHub Pages and link it in the Play Console.

Promotional screenshot requirements and instructions are in [promotional/README.md](promotional/README.md).

### Quick APK Build Summary

```bash
# 1. Generate keystore
keytool -genkey -v -keystore stocksync-release.keystore -alias stocksync -keyalg RSA -keysize 2048 -validity 10000

# 2. Create StockSync/keystore.properties (not committed to git)
#    storeFile=../stocksync-release.keystore
#    storePassword=YOUR_PASSWORD
#    keyAlias=stocksync
#    keyPassword=YOUR_PASSWORD

# 3. Build signed APK
cd StockSync && ./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Play Console Upload

1. Go to [play.google.com/console](https://play.google.com/console)
2. Create a new application, fill in listing details from `STORE_LISTING.md`
3. Upload feature graphic and screenshots to `promotional/screenshots/`
4. Complete the content rating questionnaire
5. Go to Release → Production → Create new release
6. Upload `app-release.apk` and paste release notes from `RELEASE_NOTES.md`
7. Submit for review

---

## AI Usage Summary

See `ai-usage-writeup.html` for a transparent summary (under 500 words) of how generative AI tooling was used for code generation, bug fixing, and asset creation.

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Mobile App | Kotlin + Jetpack Compose (Material 3) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Local Database | RoomDB (SQLite) with BLOB storage |
| NoSQL Database | Firebase Firestore |
| Background Sync | WorkManager |
| Preferences | DataStore + EncryptedSharedPreferences |
| Biometric Auth | AndroidX BiometricPrompt |
| SSO | Google Credential Manager |
| Barcode | Google ML Kit + CameraX |
| Push Notifications | Firebase Cloud Messaging |
| Store Location | Google Play Services Location |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil |
| API Framework | ASP.NET Core 8 Web API (C#) |
| ORM | Entity Framework Core |
| SQL Database | PostgreSQL |
| CI/CD | GitHub Actions |
| Unit Testing (App) | JUnit4 + Mockito + Robolectric |
| Unit Testing (API) | xUnit + Moq + EF Core InMemory |

