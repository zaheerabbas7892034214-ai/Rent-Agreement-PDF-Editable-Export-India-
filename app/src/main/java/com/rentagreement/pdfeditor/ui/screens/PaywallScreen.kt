package com.rentagreement.pdfeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rentagreement.pdfeditor.R
import com.rentagreement.pdfeditor.util.BillingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    billingManager: BillingManager,
    onBack: () -> Unit
) {
    val purchaseState by billingManager.purchaseState.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is BillingManager.PurchaseState.Success -> {
                // Handle success - could show a dialog or navigate back
            }
            is BillingManager.PurchaseState.AlreadyOwned -> {
                // Handle already owned
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paywall_title)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.free_features_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FeatureItem(stringResource(R.string.free_feature_1))
                    FeatureItem(stringResource(R.string.free_feature_2))
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pro_features_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.pro_badge),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FeatureItem(stringResource(R.string.pro_feature_1))
                    FeatureItem(stringResource(R.string.pro_feature_2))
                    FeatureItem(stringResource(R.string.pro_feature_3))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = stringResource(R.string.pro_price),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            when (val state = purchaseState) {
                is BillingManager.PurchaseState.Loading -> {
                    CircularProgressIndicator()
                }
                is BillingManager.PurchaseState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is BillingManager.PurchaseState.Success -> {
                    Text(
                        text = stringResource(R.string.purchase_success),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is BillingManager.PurchaseState.AlreadyOwned -> {
                    Text(
                        text = stringResource(R.string.already_owned),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {}
            }
            
            Button(
                onClick = {
                    activity?.let { billingManager.launchPurchaseFlow(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = purchaseState !is BillingManager.PurchaseState.Loading
            ) {
                Text(stringResource(R.string.purchase_button))
            }
            
            OutlinedButton(
                onClick = { billingManager.restorePurchases() },
                modifier = Modifier.fillMaxWidth(),
                enabled = purchaseState !is BillingManager.PurchaseState.Loading
            ) {
                Text(stringResource(R.string.restore_purchases))
            }
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
