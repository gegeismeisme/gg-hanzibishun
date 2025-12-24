package com.yourstudio.hskstroke.bishun.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingController(
    private val appContext: Context,
    private val productId: String,
    private val onPurchaseEntitlementChanged: (Boolean) -> Unit,
) : PurchasesUpdatedListener {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    private var productDetails: ProductDetails? = null

    fun start() {
        if (billingClient.isReady) return
        _uiState.value = _uiState.value.copy(isReady = false, lastMessage = null)
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _uiState.value = _uiState.value.copy(isReady = true)
                        queryProduct()
                        restorePurchases()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isReady = false,
                            lastMessage = "Billing unavailable (${result.responseCode})",
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _uiState.value = _uiState.value.copy(isReady = false)
                }
            },
        )
    }

    fun launchRemoveAdsPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady) {
            start()
            _uiState.value = _uiState.value.copy(lastMessage = "Billing not ready yet")
            return
        }
        if (details == null) {
            queryProduct()
            _uiState.value = _uiState.value.copy(lastMessage = "Loading product...")
            return
        }
        val offerToken = details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
        if (offerToken != null) {
            _uiState.value = _uiState.value.copy(lastMessage = "Invalid product type")
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()

        _uiState.value = _uiState.value.copy(isPurchaseInProgress = true, lastMessage = null)
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.value = _uiState.value.copy(
                isPurchaseInProgress = false,
                lastMessage = "Purchase failed (${result.responseCode})",
            )
        }
    }

    fun restorePurchases() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _uiState.value = _uiState.value.copy(
                    lastMessage = "Restore failed (${result.responseCode})",
                )
                return@queryPurchasesAsync
            }
            val hasEntitlement = purchases.any { purchase ->
                purchase.products.contains(productId) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            onPurchaseEntitlementChanged(hasEntitlement)
        }
    }

    fun destroy() {
        billingClient.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        _uiState.value = _uiState.value.copy(isPurchaseInProgress = false)
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _uiState.value = _uiState.value.copy(lastMessage = "Purchase cancelled")
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) {
            _uiState.value = _uiState.value.copy(lastMessage = "Purchase error (${result.responseCode})")
            return
        }
        purchases
            .filter { it.products.contains(productId) }
            .forEach { purchase -> handlePurchase(purchase) }
    }

    private fun queryProduct() {
        if (!billingClient.isReady) return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _uiState.value = _uiState.value.copy(
                    removeAdsPrice = null,
                    lastMessage = "Product query failed (${result.responseCode})",
                )
                return@queryProductDetailsAsync
            }
            val item = details.firstOrNull()
            productDetails = item
            val price = item?.oneTimePurchaseOfferDetails?.formattedPrice
            _uiState.value = _uiState.value.copy(removeAdsPrice = price)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.isAcknowledged) {
            onPurchaseEntitlementChanged(true)
            return
        }
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onPurchaseEntitlementChanged(true)
                _uiState.value = _uiState.value.copy(lastMessage = "Purchase activated")
            } else {
                _uiState.value = _uiState.value.copy(
                    lastMessage = "Purchase not acknowledged (${result.responseCode})",
                )
            }
        }
    }
}

