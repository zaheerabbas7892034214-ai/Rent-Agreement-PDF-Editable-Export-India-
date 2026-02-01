# Project Setup and Build Instructions

## Prerequisites

1. **Android Studio**: Giraffe (2022.3.1) or later
2. **JDK**: Version 17
3. **Android SDK**: 
   - Minimum SDK: 24 (Android 7.0)
   - Target SDK: 34 (Android 14)
   - Compile SDK: 34

## Initial Setup

### 1. Clone and Open Project

```bash
git clone <repository-url>
cd Rent-Agreement-PDF-Editable-Export-India-
```

Open the project in Android Studio using "Open an Existing Project"

### 2. Sync Gradle

Android Studio will automatically prompt you to sync Gradle. Click "Sync Now" or run:

```bash
./gradlew --refresh-dependencies
```

### 3. Install Dependencies

All dependencies are declared in `gradle/libs.versions.toml` and will be downloaded automatically during the first build:

- Jetpack Compose 2024.01.00
- Room Database 2.6.1
- Google Play Billing 6.1.0
- ML Kit Text Recognition 16.0.0
- Navigation Compose 2.7.6
- Kotlin 1.9.20

### 4. Configure Google Play Billing (Important!)

Before releasing to production:

1. Create a product in Google Play Console:
   - Product ID: `rent_pro_unlock`
   - Type: In-app product (one-time purchase)
   - Price: ₹299

2. Test billing using test accounts in Google Play Console

## Building the App

### Debug Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

Note: You'll need to configure signing keys in `app/build.gradle.kts` for release builds.

## Running the App

### Using Android Studio

1. Connect an Android device or start an emulator
2. Click the "Run" button (green triangle) or press Shift+F10
3. Select your target device

### Using Command Line

```bash
./gradlew installDebug
```

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Project Structure Verification

The complete project includes:

### Source Code (29 Kotlin files)
- ✅ MainActivity.kt - Entry point with navigation
- ✅ 7 Screen composables (Splash, Home, Import, Fields, Preview, Paywall, Settings)
- ✅ 5 ViewModels (Home, Import, Fields, Preview, Settings)
- ✅ Room Database (AppDatabase, 2 Entities, 2 DAOs)
- ✅ 2 Repositories (Document, Entitlement)
- ✅ 3 Domain Models (DocumentHistory, Entitlement, ExtractedFields)
- ✅ BillingManager - Complete billing flow with acknowledgment
- ✅ PdfProcessor - PDF extraction and OCR
- ✅ Navigation setup
- ✅ Material 3 Theme (Color, Typography, Theme)

### Resources (5 XML files)
- ✅ AndroidManifest.xml - Permissions and activities
- ✅ strings.xml - All UI strings
- ✅ colors.xml - Color palette
- ✅ themes.xml - Material theme
- ✅ file_paths.xml - FileProvider configuration

### Configuration Files
- ✅ build.gradle.kts (root)
- ✅ app/build.gradle.kts - App module configuration
- ✅ settings.gradle.kts - Project settings
- ✅ gradle.properties - Build properties
- ✅ libs.versions.toml - Dependency versions
- ✅ .gitignore - Git exclusions
- ✅ gradle wrapper files

## Architecture Verification

### MVVM Pattern ✅
- Models: Domain models in `domain/model/`
- Views: Composable screens in `ui/screens/`
- ViewModels: AndroidViewModel classes with StateFlow

### Data Layer ✅
- Room Database with entities and DAOs
- Repositories abstracting data access
- SharedPreferences as fallback for entitlements

### Domain Layer ✅
- Domain models separate from entities
- Business logic in repositories and ViewModels

### UI Layer ✅
- Jetpack Compose with Material 3
- Navigation Compose for screen flow
- State management with StateFlow

## Key Features Implemented

### Free Features ✅
- PDF selection via Storage Access Framework
- Field extraction with OCR fallback
- Editable form with validation
- Preview functionality
- Single PDF export

### Pro Features ✅
- Unlimited PDF exports
- Billing integration with Google Play v6+
- Purchase acknowledgment flow
- Restore purchases
- Entitlement persistence

### Monetization ✅
- One-time INAPP purchase (₹299)
- Product ID: rent_pro_unlock
- Entitlement management with Room + SharedPreferences
- Handle pending/cancelled/owned states

## Troubleshooting

### Gradle Sync Issues

If you encounter sync errors:

```bash
./gradlew clean
./gradlew --refresh-dependencies
```

### Build Failures

1. Verify JDK 17 is being used
2. Check Android SDK is properly installed
3. Ensure all SDK components are downloaded (SDK 34)

### Billing Issues

1. Verify product ID matches in Google Play Console
2. Test with test accounts first
3. Check billing permissions in AndroidManifest.xml

## Next Steps for Production

1. **Generate Signing Key**:
```bash
keytool -genkey -v -keystore release.keystore -alias rent_agreement_key -keyalg RSA -keysize 2048 -validity 10000
```

2. **Configure signing in build.gradle.kts**

3. **Test thoroughly**:
   - Test on multiple devices (min SDK 24 to latest)
   - Test billing flow with test accounts
   - Test OCR with various PDF formats

4. **Prepare for Release**:
   - Update versionCode and versionName
   - Add proper app icons (currently placeholders)
   - Test ProGuard rules for release build
   - Prepare privacy policy and store listing

## Support

For build issues, check:
- Android Studio logs
- Gradle console output
- `./gradlew build --stacktrace` for detailed errors

## License

Copyright © 2024. All rights reserved.
