package com.rentagreement.pdfeditor.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rentagreement.pdfeditor.R

@Composable
fun ImportConvertScreen(
    uri: String,
    fileName: String,
    onSuccess: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ImportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uri) {
        if (state is ImportViewModel.ImportState.Idle) {
            viewModel.processDocument(Uri.parse(uri), fileName)
        }
    }
    
    LaunchedEffect(state) {
        if (state is ImportViewModel.ImportState.Success) {
            onSuccess((state as ImportViewModel.ImportState.Success).documentId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.import_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        when (val currentState = state) {
            is ImportViewModel.ImportState.Processing -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = currentState.step)
            }
            is ImportViewModel.ImportState.Error -> {
                Text(
                    text = stringResource(R.string.processing_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentState.message)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        viewModel.resetState()
                        viewModel.processDocument(Uri.parse(uri), fileName)
                    }) {
                        Text(stringResource(R.string.retry_button))
                    }
                    
                    OutlinedButton(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "PDF Processing Error")
                            putExtra(Intent.EXTRA_TEXT, "Error: ${currentState.message}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Report Error"))
                    }) {
                        Text(stringResource(R.string.report_error))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.cancel))
                }
            }
            else -> {}
        }
    }
}
