package com.yourstudio.hskstroke.bishun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yourstudio.hskstroke.bishun.ui.navigation.BishunApp
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.onboarding.OnboardingScreen
import com.yourstudio.hskstroke.bishun.ui.theme.BishunTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val preferencesStore = remember { UserPreferencesStore(context.applicationContext) }
            val userPreferences by preferencesStore.data.collectAsState(initial = UserPreferences())
            val scope = rememberCoroutineScope()
            var forceShowOnboarding by rememberSaveable { mutableStateOf(false) }
            val shouldShowOnboarding = forceShowOnboarding || !userPreferences.onboardingCompleted

            BishunTheme(themeMode = userPreferences.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (shouldShowOnboarding) {
                        OnboardingScreen(
                            modifier = Modifier.fillMaxSize(),
                            onFinish = {
                                forceShowOnboarding = false
                                scope.launch { preferencesStore.setOnboardingCompleted(true) }
                            },
                            onSkip = {
                                forceShowOnboarding = false
                                scope.launch { preferencesStore.setOnboardingCompleted(true) }
                            },
                        )
                    } else {
                        BishunApp(
                            onShowOnboarding = { forceShowOnboarding = true },
                        )
                    }
                }
            }
        }
    }
}
