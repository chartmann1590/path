package com.path.app.ui.components

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.path.app.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

/**
 * Manages AdMob interstitial ads with random triggering
 *
 * Features:
 * - Pre-loads ads for smooth display
 * - Random probability-based triggering (default 30% chance)
 * - Automatic reload after ad is shown
 * - Graceful error handling
 * - Respects ad-free periods purchased with credits
 */
class AdMobInterstitialManager(
    private val activity: Activity,
    private val adUnitId: String = "ca-app-pub-8382831211800454/8304718545",
    private val triggerProbability: Float = 0.30f, // 30% chance by default
    private val userPreferences: UserPreferences? = null
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    companion object {
        private const val TAG = "AdMobInterstitial"
    }
    
    init {
        loadAd()
    }
    
    /**
     * Loads a new interstitial ad
     */
    fun loadAd() {
        if (isLoading) return
        
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isLoading = false
                    
                    // Set up callback to reload after ad is shown
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed, loading next ad")
                            interstitialAd = null
                            loadAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Ad failed to show: ${error.message}")
                            interstitialAd = null
                            loadAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed full screen content")
                            interstitialAd = null
                        }
                    }
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    // Retry after a delay (optional - you can add retry logic here)
                }
            }
        )
    }
    
    /**
     * Attempts to show an ad with random probability
     * @return true if ad was shown, false otherwise
     */
    fun tryShowAd(): Boolean {
        // Check if user is in ad-free period
        if (isAdFree()) {
            Log.d(TAG, "Ad skipped - user has ad-free time")
            return false
        }

        // Check if ad is loaded
        val ad = interstitialAd ?: return false

        // Random chance to show ad
        if (Random.nextFloat() > triggerProbability) {
            Log.d(TAG, "Ad trigger skipped (random)")
            return false
        }

        // Show the ad
        ad.show(activity)
        return true
    }

    /**
     * Checks if user is currently in an ad-free period
     */
    private fun isAdFree(): Boolean {
        return userPreferences?.let { prefs ->
            runBlocking {
                prefs.isAdFree()
            }
        } ?: false
    }
    
    /**
     * Shows an ad if loaded (bypasses random probability)
     * Useful for specific events where you always want to show an ad
     * @return true if ad was shown, false if no ad was loaded
     */
    fun showAdIfLoaded(): Boolean {
        val ad = interstitialAd ?: return false
        ad.show(activity)
        return true
    }
    
    /**
     * Checks if an ad is currently loaded
     */
    fun isAdLoaded(): Boolean = interstitialAd != null
    
    /**
     * Updates the trigger probability
     */
    fun setTriggerProbability(probability: Float) {
        require(probability in 0f..1f) { "Probability must be between 0 and 1" }
        // Note: This won't affect the current instance, but you can recreate the manager if needed
    }
}

