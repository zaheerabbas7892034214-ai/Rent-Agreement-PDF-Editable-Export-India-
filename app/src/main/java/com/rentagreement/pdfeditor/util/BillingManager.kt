package com.rentagreement.pdfeditor.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.rentagreement.pdfeditor.data.repository.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val entitlementRepository: EntitlementRepository
) : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState
    
    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        object Success : PurchaseState()
        data class Error(val message: String) : PurchaseState()
        object AlreadyOwned : PurchaseState()
    }
    
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful")
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }
    
    private fun queryPurchases() {
        scope.launch {
            try {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                
                val purchasesResult = withContext(Dispatchers.IO) {
                    billingClient?.queryPurchasesAsync(params)
                }
                
                purchasesResult?.purchasesList?.forEach { purchase ->
                    if (purchase.products.contains(PRODUCT_ID) && 
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying purchases", e)
            }
        }
    }
    
    fun launchPurchaseFlow(activity: Activity) {
        _purchaseState.value = PurchaseState.Loading
        
        scope.launch {
            try {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
                
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
                
                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient?.queryProductDetails(params)
                }
                
                val productDetails = productDetailsResult?.productDetailsList?.firstOrNull()
                
                if (productDetails != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                    
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                    
                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                } else {
                    _purchaseState.value = PurchaseState.Error("Product not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching purchase flow", e)
                _purchaseState.value = PurchaseState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PurchaseState.AlreadyOwned
                scope.launch {
                    entitlementRepository.updateProStatus(true)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Idle
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                scope.launch {
                    entitlementRepository.updateProStatus(true)
                    _purchaseState.value = PurchaseState.Success
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Loading
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        scope.launch {
            try {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                val result = withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(params)
                }
                
                if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                    entitlementRepository.updateProStatus(true)
                    _purchaseState.value = PurchaseState.Success
                } else {
                    _purchaseState.value = PurchaseState.Error("Acknowledgment failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error acknowledging purchase", e)
                _purchaseState.value = PurchaseState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun restorePurchases() {
        _purchaseState.value = PurchaseState.Loading
        queryPurchases()
    }
    
    fun resetState() {
        _purchaseState.value = PurchaseState.Idle
    }
    
    fun release() {
        billingClient?.endConnection()
    }
    
    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID = "rent_pro_unlock"
    }
}
