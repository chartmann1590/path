package com.biblereadingpath.app.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem

/**
 * Manager for AdMob Rewarded Interstitial Ads
 *
 * Handles loading and showing rewarded interstitial ads that grant users credits
 * Note: Rewarded interstitial ads appear automatically during app transitions
 */
class AdMobRewardedManager(
    private val activity: Activity,
    private val adUnitId: String,
    private val onAdRewarded: (Int) -> Unit, // Callback with credit amount
    private val onAdClosed: () -> Unit = {}
) {
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoading = false
    
    // Observable state for UI
    var isAdReadyState by mutableStateOf(false)
        private set

    companion object {
        private const val TAG = "AdMobRewarded"
        const val CREDITS_PER_AD = 10 // Credits earned per rewarded ad
    }

    init {
        Log.d(TAG, "AdMobRewardedManager initialized with adUnitId: $adUnitId")
        loadAd()
    }

    fun loadAd() {
        if (isLoading || rewardedInterstitialAd != null) {
            Log.d(TAG, "Ad already loading (isLoading=$isLoading) or loaded (rewardedInterstitialAd=${rewardedInterstitialAd != null})")
            return
        }

        Log.d(TAG, "Starting to load rewarded interstitial ad with adUnitId: $adUnitId")
        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load - Code: ${error.code}, Domain: ${error.domain}, Message: ${error.message}")
                    Log.e(TAG, "Error cause: ${error.cause?.message ?: "No cause"}")
                    rewardedInterstitialAd = null
                    isLoading = false
                    isAdReadyState = false
                }

                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded interstitial ad loaded successfully!")
                    rewardedInterstitialAd = ad
                    isLoading = false
                    isAdReadyState = true

                    // Set up callback for when ad is dismissed
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            rewardedInterstitialAd = null
                            isAdReadyState = false
                            onAdClosed()
                            // Load the next ad
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Ad failed to show - Code: ${adError.code}, Message: ${adError.message}")
                            rewardedInterstitialAd = null
                            isAdReadyState = false
                            onAdClosed()
                            loadAd()
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed full screen content")
                        }

                        override fun onAdImpression() {
                            Log.d(TAG, "Ad recorded an impression")
                        }

                        override fun onAdClicked() {
                            Log.d(TAG, "Ad was clicked")
                        }
                    }
                }
            }
        )
        Log.d(TAG, "RewardedInterstitialAd.load() called, waiting for callback...")
    }

    /**
     * Shows the rewarded interstitial ad if loaded
     * @return true if ad was shown, false otherwise
     */
    fun showAd(): Boolean {
        Log.d(TAG, "showAd() called - rewardedInterstitialAd: ${rewardedInterstitialAd != null}, isLoading: $isLoading")
        return if (rewardedInterstitialAd != null) {
            Log.d(TAG, "Showing rewarded interstitial ad")
            rewardedInterstitialAd?.show(
                activity,
                OnUserEarnedRewardListener { rewardItem ->
                    // User earned the reward
                    val rewardAmount = rewardItem.amount
                    Log.d(TAG, "User earned reward: $rewardAmount ${rewardItem.type}")

                    // Grant credits (use predefined amount for consistency)
                    onAdRewarded(CREDITS_PER_AD)
                }
            )
            true
        } else {
            Log.d(TAG, "Ad not loaded yet (rewardedInterstitialAd is null)")
            // Attempt to load if not already loading
            if (!isLoading) {
                Log.d(TAG, "Attempting to load ad since not currently loading")
                loadAd()
            } else {
                Log.d(TAG, "Ad is already loading, skipping load request")
            }
            false
        }
    }

    fun isAdReady(): Boolean {
        val ready = rewardedInterstitialAd != null
        // Keep state in sync
        if (isAdReadyState != ready) {
            isAdReadyState = ready
        }
        Log.d(TAG, "isAdReady() called - returning: $ready (rewardedInterstitialAd: ${rewardedInterstitialAd != null}, isLoading: $isLoading)")
        return ready
    }
}
