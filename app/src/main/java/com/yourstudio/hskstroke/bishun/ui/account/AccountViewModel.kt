package com.yourstudio.hskstroke.bishun.ui.account

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.monetization.AdsController
import com.yourstudio.hskstroke.bishun.monetization.AdsUiState
import com.yourstudio.hskstroke.bishun.monetization.BillingController
import com.yourstudio.hskstroke.bishun.monetization.BillingProducts
import com.yourstudio.hskstroke.bishun.monetization.BillingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    appContext: Context,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val billingController = BillingController(
        appContext = appContext,
        productId = BillingProducts.REMOVE_ADS,
        onPurchaseEntitlementChanged = { entitled ->
            viewModelScope.launch {
                userPreferencesStore.setRemoveAdsPurchased(entitled)
            }
        },
    )

    private val adsController = AdsController(appContext)

    val billingUiState: StateFlow<BillingUiState> = billingController.uiState
    val adsUiState: StateFlow<AdsUiState> = adsController.uiState

    private val _rewardEligible = MutableStateFlow(false)
    val rewardEligible: StateFlow<Boolean> = _rewardEligible.asStateFlow()

    init {
        billingController.start()
        adsController.initialize()
        viewModelScope.launch {
            delay(REWARDED_ELIGIBLE_DELAY_MS)
            _rewardEligible.value = true
        }
    }

    fun purchaseRemoveAds(activity: Activity) {
        billingController.launchRemoveAdsPurchase(activity)
    }

    fun restorePurchases() {
        billingController.restorePurchases()
    }

    fun showAppOpenAd(activity: Activity) {
        adsController.showAppOpenAd(activity)
    }

    fun watchRewardedAd(activity: Activity) {
        adsController.showRewardedAd(
            activity = activity,
            onReward = {
                val now = System.currentTimeMillis()
                viewModelScope.launch {
                    userPreferencesStore.setRewardedAdFreeUntil(now + REWARDED_AD_FREE_MS)
                }
            },
        )
    }

    fun setRemoveAdsPurchased(purchased: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setRemoveAdsPurchased(purchased)
        }
    }

    override fun onCleared() {
        billingController.destroy()
        super.onCleared()
    }

    companion object {
        private const val REWARDED_AD_FREE_MS: Long = 30 * 60 * 1000L
        private const val REWARDED_ELIGIBLE_DELAY_MS: Long = 2 * 60 * 1000L

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val userPreferencesStore = UserPreferencesStore(appContext)
                    return AccountViewModel(
                        appContext = appContext,
                        userPreferencesStore = userPreferencesStore,
                    ) as T
                }
            }
        }
    }
}
