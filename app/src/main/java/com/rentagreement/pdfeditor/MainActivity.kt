package com.rentagreement.pdfeditor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rentagreement.pdfeditor.data.repository.EntitlementRepository
import com.rentagreement.pdfeditor.ui.navigation.Screen
import com.rentagreement.pdfeditor.ui.screens.*
import com.rentagreement.pdfeditor.ui.theme.RentAgreementPDFEditorTheme
import com.rentagreement.pdfeditor.util.BillingManager
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    
    private lateinit var billingManager: BillingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val entitlementRepository = EntitlementRepository(applicationContext)
        billingManager = BillingManager(applicationContext, entitlementRepository)
        billingManager.initialize()
        
        setContent {
            RentAgreementPDFEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RentAgreementApp(billingManager)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingManager.release()
    }
}

@Composable
fun RentAgreementApp(billingManager: BillingManager) {
    val navController = rememberNavController()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedFileName = getFileName(it)
            val encodedUri = URLEncoder.encode(it.toString(), StandardCharsets.UTF_8.toString())
            navController.navigate(Screen.ImportConvert.createRoute(encodedUri))
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onSelectPdf = {
                    pdfPickerLauncher.launch("application/pdf")
                },
                onDocumentClick = { documentId ->
                    navController.navigate(Screen.ExtractedFields.createRoute(documentId))
                },
                onUpgradeClick = {
                    navController.navigate(Screen.Paywall.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.ImportConvert.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("uri") ?: ""
            val uri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
            ImportConvertScreen(
                uri = uri,
                fileName = selectedFileName,
                onSuccess = { documentId ->
                    navController.navigate(Screen.ExtractedFields.createRoute(documentId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.ExtractedFields.route,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L
            ExtractedFieldsScreen(
                documentId = documentId,
                onBack = {
                    navController.popBackStack()
                },
                onGenerateClick = { docId ->
                    navController.navigate(Screen.PreviewPDF.createRoute(docId))
                }
            )
        }
        
        composable(
            route = Screen.PreviewPDF.route,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L
            PreviewPDFScreen(
                documentId = documentId,
                onBack = {
                    navController.popBackStack()
                },
                onUpgradeClick = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }
        
        composable(Screen.Paywall.route) {
            PaywallScreen(
                billingManager = billingManager,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                billingManager = billingManager,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun getFileName(uri: Uri): String {
    val path = uri.path ?: return "document.pdf"
    return path.substringAfterLast('/').ifEmpty { "document.pdf" }
}
