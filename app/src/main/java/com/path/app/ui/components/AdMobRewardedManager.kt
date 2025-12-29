package com.path.app.ui.components

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manager for AdMob Rewarded Ads
 *
 * Handles loading and showing rewarded ads that grant users credits
 */
class AdMobRewardedManager(
    private val activity: Activity,
    private val adUnitId: String,
    private val onAdRewarded: (Int) -> Unit, // Callback with credit amount
    private val onAdClosed: () -> Unit = {}
) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    companion object {
        private const val TAG = "AdMobRewarded"
        const val CREDITS_PER_AD = 10 // Credits earned per rewarded ad
    }

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || rewardedAd != null) {
            Log.d(TAG, "Ad already loading or loaded")
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad loaded successfully")
                    rewardedAd = ad
                    isLoading = false

                    // Set up callback for when ad is dismissed
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            rewardedAd = null
                            onAdClosed()
                            // Load the next ad
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Ad failed to show: ${adError.message}")
                            rewardedAd = null
                            onAdClosed()
                            loadAd()
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed full screen content")
                        }
                    }
                }
            }
        )
    }

    /**
     * Shows the rewarded ad if loaded
     * @return true if ad was shown, false otherwise
     */
    fun showAd(): Boolean {
        return if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                // User earned the reward
                val rewardAmount = rewardItem.amount
                Log.d(TAG, "User earned reward: $rewardAmount ${rewardItem.type}")

                // Grant credits (use predefined amount for consistency)
                onAdRewarded(CREDITS_PER_AD)
            }
            true
        } else {
            Log.d(TAG, "Ad not loaded yet")
            // Attempt to load if not already loading
            if (!isLoading) {
                loadAd()
            }
            false
        }
    }

    fun isAdReady(): Boolean = rewardedAd != null
}
