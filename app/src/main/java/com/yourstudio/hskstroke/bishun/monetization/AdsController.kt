package com.yourstudio.hskstroke.bishun.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.yourstudio.hskstroke.bishun.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdsController(private val appContext: Context) {

    private val _uiState = MutableStateFlow(AdsUiState())
    val uiState: StateFlow<AdsUiState> = _uiState.asStateFlow()

    private var appOpenAd: AppOpenAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isShowingFullScreen = false
    private var initializing = false

    fun initialize() {
        if (_uiState.value.isInitialized || initializing) return
        initializing = true
        MobileAds.initialize(appContext) {
            initializing = false
            _uiState.value = _uiState.value.copy(isInitialized = true)
            preloadAppOpenAd()
            preloadRewardedAd()
        }
    }

    fun preloadAppOpenAd() {
        if (!_uiState.value.isInitialized) return
        if (appOpenAd != null || isShowingFullScreen) return
        val adUnitId = appContext.getString(R.string.admob_app_open_unit_id)
        AppOpenAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    _uiState.value = _uiState.value.copy(isAppOpenAdReady = true, lastMessage = null)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    _uiState.value = _uiState.value.copy(
                        isAppOpenAdReady = false,
                        lastMessage = "AppOpen failed: ${error.code}",
                    )
                }
            },
        )
    }

    fun showAppOpenAd(activity: Activity, onDismissed: (() -> Unit)? = null) {
        val ad = appOpenAd ?: run {
            preloadAppOpenAd()
            return
        }
        if (isShowingFullScreen) return
        isShowingFullScreen = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingFullScreen = false
                _uiState.value = _uiState.value.copy(isAppOpenAdReady = false)
                preloadAppOpenAd()
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingFullScreen = false
                _uiState.value = _uiState.value.copy(
                    isAppOpenAdReady = false,
                    lastMessage = "AppOpen show failed: ${error.code}",
                )
                preloadAppOpenAd()
                onDismissed?.invoke()
            }
        }
        ad.show(activity)
    }

    fun preloadRewardedAd() {
        if (!_uiState.value.isInitialized) return
        if (rewardedAd != null || isShowingFullScreen) return
        val adUnitId = appContext.getString(R.string.admob_rewarded_unit_id)
        RewardedAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _uiState.value = _uiState.value.copy(isRewardedAdReady = true, lastMessage = null)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    _uiState.value = _uiState.value.copy(
                        isRewardedAdReady = false,
                        lastMessage = "Rewarded failed: ${error.code}",
                    )
                }
            },
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onReward: () -> Unit,
        onDismissed: (() -> Unit)? = null,
    ) {
        val ad = rewardedAd ?: run {
            preloadRewardedAd()
            return
        }
        if (isShowingFullScreen) return
        isShowingFullScreen = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isShowingFullScreen = false
                _uiState.value = _uiState.value.copy(isRewardedAdReady = false)
                preloadRewardedAd()
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                isShowingFullScreen = false
                _uiState.value = _uiState.value.copy(
                    isRewardedAdReady = false,
                    lastMessage = "Rewarded show failed: ${error.code}",
                )
                preloadRewardedAd()
                onDismissed?.invoke()
            }
        }
        ad.show(activity) { onReward() }
    }
}

