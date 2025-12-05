package com.example.bishun.ui.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountUiState(
    val isSignedIn: Boolean = false,
    val unlockedLevels: Set<Int> = emptySet(),
) {
    val hasPremiumAccess: Boolean get() = unlockedLevels.isNotEmpty()
}

class AccountViewModel(
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    val uiState: StateFlow<AccountUiState> = userPreferencesStore.data
        .map { prefs ->
            AccountUiState(
                isSignedIn = prefs.isAccountSignedIn,
                unlockedLevels = prefs.unlockedCourseLevels,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountUiState(),
        )

    fun signIn() {
        viewModelScope.launch {
            userPreferencesStore.setAccountSignedIn(true)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userPreferencesStore.setAccountSignedIn(false)
        }
    }

    fun unlockPremiumLevels(levels: Set<Int> = PREMIUM_LEVELS) {
        viewModelScope.launch {
            userPreferencesStore.unlockCourseLevels(levels)
        }
    }

    companion object {
        val FREE_LEVELS = setOf(1)
        val PREMIUM_LEVELS = setOf(2, 3, 4, 5, 6)

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val store = UserPreferencesStore(appContext)
                    return AccountViewModel(store) as T
                }
            }
        }
    }
}
