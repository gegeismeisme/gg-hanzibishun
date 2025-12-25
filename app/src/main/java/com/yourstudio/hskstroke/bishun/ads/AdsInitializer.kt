package com.yourstudio.hskstroke.bishun.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

object AdsInitializer {
    @Volatile
    private var initialized: Boolean = false

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            val requestConfiguration = RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .build()

            MobileAds.setRequestConfiguration(requestConfiguration)
            MobileAds.initialize(context.applicationContext)
            initialized = true
        }
    }
}

