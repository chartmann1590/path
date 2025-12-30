package com.biblereadingpath.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val credits = userPreferences.adCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val adFreeUntil = userPreferences.adFreeUntil
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val currentTranslation: StateFlow<String> = userPreferences.translation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "web")

    var showRewardConfirmation by mutableStateOf(false)
        private set

    var showInsufficientCreditsDialog by mutableStateOf(false)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    fun onAdWatched(creditsEarned: Int) {
        viewModelScope.launch {
            userPreferences.addCredits(creditsEarned)
            showRewardConfirmation = true
        }
    }

    fun dismissRewardConfirmation() {
        showRewardConfirmation = false
    }

    fun dismissInsufficientCreditsDialog() {
        showInsufficientCreditsDialog = false
    }

    /**
     * Purchase ad-free time with credits
     * @param hours Number of hours of ad-free time
     * @param cost Credit cost
     */
    fun purchaseAdFreeTime(hours: Int, cost: Int) {
        viewModelScope.launch {
            isProcessing = true
            val success = userPreferences.spendCredits(cost)

            if (success) {
                // Calculate expiry time
                val currentTime = System.currentTimeMillis()
                val expiryTime = currentTime + (hours * 60 * 60 * 1000L)

                // Extend existing ad-free time if still active
                val existingExpiry = adFreeUntil.value
                val finalExpiry = if (existingExpiry > currentTime) {
                    // Add to existing time
                    existingExpiry + (hours * 60 * 60 * 1000L)
                } else {
                    // Start new ad-free period
                    expiryTime
                }

                userPreferences.setAdFreeUntil(finalExpiry)
            } else {
                showInsufficientCreditsDialog = true
            }

            isProcessing = false
        }
    }

    fun isAdFree(): Boolean {
        return System.currentTimeMillis() < adFreeUntil.value
    }

    fun getRemainingAdFreeTime(): Long {
        val remaining = adFreeUntil.value - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }
}
