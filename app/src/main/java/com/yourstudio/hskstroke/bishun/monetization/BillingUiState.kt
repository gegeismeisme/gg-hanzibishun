package com.yourstudio.hskstroke.bishun.monetization

data class BillingUiState(
    val isReady: Boolean = false,
    val removeAdsPrice: String? = null,
    val isPurchaseInProgress: Boolean = false,
    val lastMessage: String? = null,
)

