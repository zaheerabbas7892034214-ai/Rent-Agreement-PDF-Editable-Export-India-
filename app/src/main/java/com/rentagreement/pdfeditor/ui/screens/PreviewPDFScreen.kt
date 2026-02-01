package com.rentagreement.pdfeditor.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rentagreement.pdfeditor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewPDFScreen(
    documentId: Long,
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    viewModel: PreviewViewModel = viewModel()
) {
    val document by viewModel.document.collectAsState()
    val entitlement by viewModel.entitlement.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }
    
    LaunchedEffect(exportState) {
        if (exportState is PreviewViewModel.ExportState.Success) {
            val file = (exportState as PreviewViewModel.ExportState.Success).file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
            viewModel.resetExportState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preview_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!entitlement.isPro && entitlement.freeExportsRemaining <= 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.free_limit_reached),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onUpgradeClick) {
                            Text(stringResource(R.string.upgrade_to_pro))
                        }
                    }
                }
            } else if (!entitlement.isPro) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.unlock_pro_banner),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onUpgradeClick) {
                            Text(stringResource(R.string.upgrade_to_pro))
                        }
                    }
                }
            }
            
            document?.let { doc ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Preview: ${doc.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RENT AGREEMENT SUMMARY",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                            
                            doc.extractedFields.let { fields ->
                                if (fields.ownerName.isNotEmpty()) {
                                    Text("Owner: ${fields.ownerName}")
                                }
                                if (fields.tenantName.isNotEmpty()) {
                                    Text("Tenant: ${fields.tenantName}")
                                }
                                if (fields.propertyAddress.isNotEmpty()) {
                                    Text("Property: ${fields.propertyAddress}")
                                }
                                if (fields.rentAmount.isNotEmpty()) {
                                    Text("Rent: ₹${fields.rentAmount}")
                                }
                                if (fields.depositAmount.isNotEmpty()) {
                                    Text("Deposit: ₹${fields.depositAmount}")
                                }
                                if (fields.agreementStartDate.isNotEmpty()) {
                                    Text("Start Date: ${fields.agreementStartDate}")
                                }
                                if (fields.durationMonths.isNotEmpty()) {
                                    Text("Duration: ${fields.durationMonths} months")
                                }
                                if (fields.noticePeriod.isNotEmpty()) {
                                    Text("Notice Period: ${fields.noticePeriod} days")
                                }
                                if (fields.maintenanceAmount.isNotEmpty()) {
                                    Text("Maintenance: ₹${fields.maintenanceAmount}")
                                }
                            }
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (val state = exportState) {
                    is PreviewViewModel.ExportState.Generating -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is PreviewViewModel.ExportState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
                
                val canExport = entitlement.isPro || entitlement.freeExportsRemaining > 0
                
                Button(
                    onClick = { viewModel.exportPdf() },
                    enabled = canExport && exportState !is PreviewViewModel.ExportState.Generating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_pdf))
                }
            }
        }
    }
}
