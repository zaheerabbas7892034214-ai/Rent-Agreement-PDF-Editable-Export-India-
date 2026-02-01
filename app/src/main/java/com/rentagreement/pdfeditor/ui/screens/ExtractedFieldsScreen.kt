package com.rentagreement.pdfeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rentagreement.pdfeditor.R
import com.rentagreement.pdfeditor.domain.model.ExtractedFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractedFieldsScreen(
    documentId: Long,
    onBack: () -> Unit,
    onGenerateClick: (Long) -> Unit,
    viewModel: ExtractedFieldsViewModel = viewModel()
) {
    val fields by viewModel.fields.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.extracted_fields_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fields.ownerName,
                onValueChange = { viewModel.updateFields(fields.copy(ownerName = it)) },
                label = { Text(stringResource(R.string.owner_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.tenantName,
                onValueChange = { viewModel.updateFields(fields.copy(tenantName = it)) },
                label = { Text(stringResource(R.string.tenant_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.propertyAddress,
                onValueChange = { viewModel.updateFields(fields.copy(propertyAddress = it)) },
                label = { Text(stringResource(R.string.property_address)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            OutlinedTextField(
                value = fields.rentAmount,
                onValueChange = { viewModel.updateFields(fields.copy(rentAmount = it)) },
                label = { Text(stringResource(R.string.rent_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.depositAmount,
                onValueChange = { viewModel.updateFields(fields.copy(depositAmount = it)) },
                label = { Text(stringResource(R.string.deposit_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.agreementStartDate,
                onValueChange = { viewModel.updateFields(fields.copy(agreementStartDate = it)) },
                label = { Text(stringResource(R.string.agreement_start_date)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.durationMonths,
                onValueChange = { viewModel.updateFields(fields.copy(durationMonths = it)) },
                label = { Text(stringResource(R.string.duration_months)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.noticePeriod,
                onValueChange = { viewModel.updateFields(fields.copy(noticePeriod = it)) },
                label = { Text(stringResource(R.string.notice_period)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = fields.maintenanceAmount,
                onValueChange = { viewModel.updateFields(fields.copy(maintenanceAmount = it)) },
                label = { Text(stringResource(R.string.maintenance_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    viewModel.saveAndGeneratePdf(
                        onSuccess = onGenerateClick,
                        onError = { msg ->
                            errorMessage = msg
                            showError = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.generate_pdf_button))
            }
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
