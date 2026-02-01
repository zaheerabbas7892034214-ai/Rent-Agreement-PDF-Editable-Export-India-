# Rent Agreement PDF Editor (India)

A production-ready Android application for extracting, editing, and generating rent agreement PDFs in India.

## Features

### Free Features
- Preview and extract key fields from rent agreement PDFs
- OCR support for scanned documents
- Export PDF once (free limit)

### Pro Features (₹299 one-time payment)
- Unlimited PDF exports
- Save templates
- Unlimited edits

## Technical Stack

- **Platform**: Android (Min SDK 24, Target SDK 34)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **Billing**: Google Play Billing v6+
- **PDF Processing**: PdfRenderer + ML Kit Text Recognition
- **File Handling**: Storage Access Framework (SAF)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/rentagreement/pdfeditor/
│   │   ├── data/
│   │   │   ├── database/      # Room entities, DAOs, database
│   │   │   └── repository/    # Repository layer
│   │   ├── domain/
│   │   │   └── model/         # Domain models
│   │   ├── ui/
│   │   │   ├── navigation/    # Navigation setup
│   │   │   ├── screens/       # Compose screens & ViewModels
│   │   │   └── theme/         # Material 3 theme
│   │   ├── util/              # Utilities (Billing, PDF processing)
│   │   └── MainActivity.kt
│   ├── res/                   # Resources (strings, themes, etc.)
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Screens

1. **SplashScreen** - App initialization and entitlement check
2. **HomeScreen** - Document list and PDF selection
3. **ImportConvertScreen** - PDF processing with progress indicators
4. **ExtractedFieldsScreen** - Editable form with validation
5. **PreviewPDFScreen** - PDF preview and export
6. **PaywallScreen** - Pro upgrade with billing integration
7. **SettingsScreen** - Restore purchases, clear history, privacy policy

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```

## Requirements

- Android Studio Giraffe or later
- JDK 17
- Android SDK 34

## Monetization

The app uses Google Play Billing with one-time in-app purchase:
- Product ID: `rent_pro_unlock`
- Price: ₹299

Entitlement is managed via Room database with SharedPreferences fallback for persistence.

## License

Copyright © 2024. All rights reserved.
