#!/bin/bash

# Project Validation Script
# Verifies that all necessary files for Android project are present

echo "==================================="
echo "Project Structure Validation"
echo "==================================="
echo ""

BASE_DIR="/home/runner/work/Rent-Agreement-PDF-Editable-Export-India-/Rent-Agreement-PDF-Editable-Export-India-"
cd "$BASE_DIR"

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (MISSING)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        return 0
    else
        echo -e "${RED}✗${NC} $1/ (MISSING)"
        return 1
    fi
}

echo "Checking Gradle Configuration..."
check_file "build.gradle.kts"
check_file "settings.gradle.kts"
check_file "gradle.properties"
check_file "gradle/libs.versions.toml"
check_file "gradle/wrapper/gradle-wrapper.properties"
check_file "gradlew"
echo ""

echo "Checking App Module..."
check_file "app/build.gradle.kts"
check_file "app/src/main/AndroidManifest.xml"
echo ""

echo "Checking Source Code - Main Components..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/MainActivity.kt"
echo ""

echo "Checking Data Layer..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/database/AppDatabase.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/database/DocumentHistoryEntity.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/database/EntitlementEntity.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/database/DocumentHistoryDao.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/database/EntitlementDao.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/repository/DocumentRepository.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/data/repository/EntitlementRepository.kt"
echo ""

echo "Checking Domain Layer..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/domain/model/DocumentHistory.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/domain/model/Entitlement.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/domain/model/ExtractedFields.kt"
echo ""

echo "Checking UI Layer - Screens..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/SplashScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/HomeScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/HomeViewModel.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/ImportConvertScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/ImportViewModel.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/ExtractedFieldsScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/ExtractedFieldsViewModel.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/PreviewPDFScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/PreviewViewModel.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/PaywallScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/SettingsScreen.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/screens/SettingsViewModel.kt"
echo ""

echo "Checking UI Layer - Theme..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/theme/Color.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/theme/Type.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/theme/Theme.kt"
echo ""

echo "Checking UI Layer - Navigation..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/ui/navigation/Navigation.kt"
echo ""

echo "Checking Utilities..."
check_file "app/src/main/java/com/rentagreement/pdfeditor/util/BillingManager.kt"
check_file "app/src/main/java/com/rentagreement/pdfeditor/util/PdfProcessor.kt"
echo ""

echo "Checking Resources..."
check_file "app/src/main/res/values/strings.xml"
check_file "app/src/main/res/values/colors.xml"
check_file "app/src/main/res/values/themes.xml"
check_file "app/src/main/res/xml/file_paths.xml"
echo ""

echo "Checking Documentation..."
check_file "README.md"
check_file "BUILD_INSTRUCTIONS.md"
check_file ".gitignore"
echo ""

# Count files
echo "==================================="
echo "Summary"
echo "==================================="
KOTLIN_COUNT=$(find app/src -name "*.kt" 2>/dev/null | wc -l)
XML_COUNT=$(find app/src -name "*.xml" 2>/dev/null | wc -l)

echo "Kotlin source files: $KOTLIN_COUNT"
echo "XML resource files: $XML_COUNT"
echo ""

echo -e "${GREEN}✓ Project structure validation complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Open project in Android Studio"
echo "2. Let Gradle sync complete"
echo "3. Build the project: ./gradlew assembleDebug"
echo "4. Run on device or emulator"
