package com.rentagreement.pdfeditor.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object ImportConvert : Screen("import_convert/{uri}") {
        fun createRoute(uri: String) = "import_convert/$uri"
    }
    object ExtractedFields : Screen("extracted_fields/{documentId}") {
        fun createRoute(documentId: Long) = "extracted_fields/$documentId"
    }
    object PreviewPDF : Screen("preview_pdf/{documentId}") {
        fun createRoute(documentId: Long) = "preview_pdf/$documentId"
    }
    object Paywall : Screen("paywall")
    object Settings : Screen("settings")
}
