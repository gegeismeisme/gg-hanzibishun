package com.yourstudio.hskstroke.bishun.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class InAppProduct(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String?,
)

data class BillingUiState(
    val isConnecting: Boolean = false,
    val isReady: Boolean = false,
    val hasPendingPurchase: Boolean = false,
    val proProduct: InAppProduct? = null,
    val lastResponseCode: Int? = null,
)

class BillingRepository(
    context: Context,
    private val preferencesStore: UserPreferencesStore,
) {
    private val appContext = context.applicationContext
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main.immediate)
    private val productDetailsById: MutableMap<String, ProductDetails> = mutableMapOf()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        scope.launch {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases)
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                preferencesStore.setBillingLastErrorCode(billingResult.responseCode)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    fun start() {
        if (_uiState.value.isReady || _uiState.value.isConnecting) return
        _uiState.value = _uiState.value.copy(isConnecting = true)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val ok = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isReady = ok,
                    lastResponseCode = billingResult.responseCode,
                )
                if (ok) {
                    scope.launch {
                        refreshProductDetails()
                        refreshPurchases()
                    }
                } else {
                    scope.launch { preferencesStore.setBillingLastErrorCode(billingResult.responseCode) }
                }
            }

            override fun onBillingServiceDisconnected() {
                _uiState.value = _uiState.value.copy(isReady = false, isConnecting = false)
            }
        })
    }

    fun end() {
        billingClient.endConnection()
        job.cancel()
    }

    fun restorePurchases() {
        scope.launch {
            if (!billingClient.isReady) start()
            refreshPurchases()
        }
    }

    fun launchProPurchase(activity: Activity) {
        launchPurchase(activity, BillingProducts.ProLifetime)
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val productDetails = productDetailsById[productId] ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build(),
                ),
            )
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        _uiState.value = _uiState.value.copy(lastResponseCode = result.responseCode)
    }

    private suspend fun refreshProductDetails() {
        if (!billingClient.isReady) return

        val productList = BillingProducts.inAppProducts.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val (billingResult, productDetailsList) = withContext(Dispatchers.IO) {
            suspendQueryProductDetails(params)
        }

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            preferencesStore.setBillingLastErrorCode(billingResult.responseCode)
            _uiState.value = _uiState.value.copy(lastResponseCode = billingResult.responseCode)
            return
        }

        productDetailsById.clear()
        productDetailsList.forEach { details ->
            productDetailsById[details.productId] = details
        }

        val pro = productDetailsById[BillingProducts.ProLifetime]?.toInAppProduct()
        _uiState.value = _uiState.value.copy(proProduct = pro, lastResponseCode = billingResult.responseCode)
    }

    private suspend fun refreshPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val (billingResult, purchases) = withContext(Dispatchers.IO) {
            suspendQueryPurchases(params)
        }

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(purchases)
            return
        }

        preferencesStore.setBillingLastErrorCode(billingResult.responseCode)
        _uiState.value = _uiState.value.copy(lastResponseCode = billingResult.responseCode)
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        val purchased = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val pending = purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        _uiState.value = _uiState.value.copy(hasPendingPurchase = pending)

        purchased.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }

        val entitledProducts = purchased
            .flatMap { it.products }
            .distinct()
        val hasPro = entitledProducts.contains(BillingProducts.ProLifetime)

        preferencesStore.updateProEntitlement(
            entitled = hasPro,
            products = entitledProducts,
            syncedAtEpochMs = System.currentTimeMillis(),
        )
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (!billingClient.isReady) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val billingResult = withContext(Dispatchers.IO) {
            suspendAcknowledgePurchase(params)
        }
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            preferencesStore.setBillingLastErrorCode(billingResult.responseCode)
            _uiState.value = _uiState.value.copy(lastResponseCode = billingResult.responseCode)
        }
    }

    private suspend fun suspendQueryPurchases(
        params: QueryPurchasesParams,
    ): Pair<BillingResult, List<Purchase>> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (continuation.isActive) {
                    continuation.resume(Pair(billingResult, purchases))
                }
            }
        }
    }

    private suspend fun suspendQueryProductDetails(
        params: QueryProductDetailsParams,
    ): Pair<BillingResult, List<ProductDetails>> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (continuation.isActive) {
                    continuation.resume(Pair(billingResult, productDetailsList))
                }
            }
        }
    }

    private suspend fun suspendAcknowledgePurchase(params: AcknowledgePurchaseParams): BillingResult =
        withContext(Dispatchers.IO) {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (continuation.isActive) {
                        continuation.resume(billingResult)
                    }
                }
            }
        }
}

private fun ProductDetails.toInAppProduct(): InAppProduct {
    val offer = oneTimePurchaseOfferDetails
    return InAppProduct(
        productId = productId,
        title = title,
        description = description,
        formattedPrice = offer?.formattedPrice,
    )
}
