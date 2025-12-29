# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Charles\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# and each project's build.gradle file.

# Jetpack Compose
-keep class androidx.compose.material3.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.dao.Dao

# Gson (used for loading local JSON)
-keep class com.path.app.domain.model.** { *; }
-keep class com.google.gson.** { *; }
