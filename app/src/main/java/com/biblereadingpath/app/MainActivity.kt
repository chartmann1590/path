package com.biblereadingpath.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import com.biblereadingpath.app.data.preferences.UserPreferences
import com.biblereadingpath.app.ui.PathApp
import com.biblereadingpath.app.ui.components.AdMobInterstitialManager
import com.biblereadingpath.app.ui.components.AdMobRewardedManager
import com.biblereadingpath.app.ui.theme.PathTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var navigationBook by mutableStateOf<String?>(null)
    private var navigationChapter by mutableStateOf<Int?>(null)

    // AdMob Interstitial Manager - initialized after MobileAds
    private var interstitialAdManagerState = mutableStateOf<AdMobInterstitialManager?>(null)

    // AdMob Rewarded Manager - initialized after MobileAds
    private var rewardedAdManagerState = mutableStateOf<AdMobRewardedManager?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Initialize user preferences for rewards
        val userPreferences = UserPreferences(this)

        // Initialize interstitial ad manager
        // Note: MobileAds.initialize is synchronous, so we can create the manager immediately
        interstitialAdManagerState.value = AdMobInterstitialManager(
            activity = this,
            adUnitId = "ca-app-pub-8382831211800454/8304718545",
            triggerProbability = 0.30f, // 30% chance to show ad
            userPreferences = userPreferences
        )

        // Initialize rewarded ad manager
        rewardedAdManagerState.value = AdMobRewardedManager(
            activity = this,
            adUnitId = "ca-app-pub-8382831211800454/2959417745",
            onAdRewarded = { credits ->
                // Grant credits to user
                CoroutineScope(Dispatchers.IO).launch {
                    userPreferences.addCredits(credits)
                }
            }
        )

        // Check if we came from a notification
        navigationBook = intent?.getStringExtra("navigate_to_book")
        navigationChapter = intent?.getIntExtra("navigate_to_chapter", -1)?.takeIf { it != -1 }

        setContent {
            PathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PathApp(
                        initialBook = navigationBook,
                        initialChapter = navigationChapter,
                        interstitialAdManager = interstitialAdManagerState.value,
                        rewardedAdManager = rewardedAdManagerState.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        // Handle when app is already running and notification is clicked
        navigationBook = intent?.getStringExtra("navigate_to_book")
        navigationChapter = intent?.getIntExtra("navigate_to_chapter", -1)?.takeIf { it != -1 }
    }
}
