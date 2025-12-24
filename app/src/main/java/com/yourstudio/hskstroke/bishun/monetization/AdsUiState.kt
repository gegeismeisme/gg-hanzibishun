package com.yourstudio.hskstroke.bishun.monetization

data class AdsUiState(
    val isInitialized: Boolean = false,
    val isAppOpenAdReady: Boolean = false,
    val isRewardedAdReady: Boolean = false,
    val lastMessage: String? = null,
)

