import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun propertyOrEnv(propertyName: String, envName: String, defaultValue: String = ""): String =
    localProperties.getProperty(propertyName) ?: System.getenv(envName) ?: defaultValue

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val releaseStoreFilePath = propertyOrEnv("release.store.file", "RELEASE_STORE_FILE", "keystore/release.keystore")
val releaseStorePassword = propertyOrEnv("release.store.password", "RELEASE_STORE_PASSWORD")
val releaseKeyAlias = propertyOrEnv("release.key.alias", "RELEASE_KEY_ALIAS")
val releaseKeyPassword = propertyOrEnv("release.key.password", "RELEASE_KEY_PASSWORD")

android {
    namespace = "com.biblereadingpath.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.biblereadingpath.app"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("ANDROID_VERSION_CODE") ?: "3").toInt()
        versionName = System.getenv("ANDROID_VERSION_NAME") ?: "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Enable multidex for release
        multiDexEnabled = true

        val githubToken = propertyOrEnv("github.api.token", "GH_API_TOKEN")
        val githubOwner = propertyOrEnv("github.repo.owner", "GH_REPO_OWNER")
        val githubRepo = propertyOrEnv("github.repo.name", "GH_REPO_NAME")
        val admobApplicationId = propertyOrEnv("admob.application.id", "ADMOB_APPLICATION_ID", "ca-app-pub-3940256099942544~3347511713")
        val admobBannerAdUnitId = propertyOrEnv("admob.banner.ad.unit.id", "ADMOB_BANNER_AD_UNIT_ID")
        val admobInterstitialAdUnitId = propertyOrEnv("admob.interstitial.ad.unit.id", "ADMOB_INTERSTITIAL_AD_UNIT_ID")
        val admobRewardedAdUnitId = propertyOrEnv("admob.rewarded.ad.unit.id", "ADMOB_REWARDED_AD_UNIT_ID")

        manifestPlaceholders["admobApplicationId"] = admobApplicationId
        buildConfigField("String", "GITHUB_API_TOKEN", buildConfigString(githubToken))
        buildConfigField("String", "GITHUB_REPO_OWNER", buildConfigString(githubOwner))
        buildConfigField("String", "GITHUB_REPO_NAME", buildConfigString(githubRepo))
        buildConfigField("String", "FEEDBACK_ASSETS_DIR", "\"feedback-assets\"")
        buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", buildConfigString(admobBannerAdUnitId))
        buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", buildConfigString(admobInterstitialAdUnitId))
        buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", buildConfigString(admobRewardedAdUnitId))
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(releaseStoreFilePath)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            // Ensure Firebase Analytics is enabled in release
            buildConfigField("boolean", "ANALYTICS_ENABLED", "true")
        }
        debug {
            buildConfigField("boolean", "ANALYTICS_ENABLED", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xskip-metadata-version-check")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    // Glance (Widgets)
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")

    // Retrofit (for Ollama if needed, or just use Ktor/OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // LiteRT-LM for on-device Gemma 4
    implementation("com.google.ai.edge.litertlm:litertlm-android:latest.release")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    // Gson for backup/restore
    implementation("com.google.code.gson:gson:2.10.1")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Firebase Performance Monitoring (optional but recommended)
    implementation("com.google.firebase:firebase-perf-ktx")

    // Google AdMob
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
