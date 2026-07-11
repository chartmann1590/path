package com.biblereadingpath.app.data.feedback

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import com.biblereadingpath.app.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

object DiagnosticsHelper {
    fun collectDiagnostics(context: Context): String {
        val info = buildString {
            appendLine("## Diagnostics")
            appendLine()
            appendLine("- App: Path")
            appendLine("- Package: ${context.packageName}")
            appendLine("- Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("- Device: ${Build.BRAND} ${Build.MODEL}")
            appendLine("- Manufacturer: ${Build.MANUFACTURER}")
            appendLine("- Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
            appendLine("- Locale: ${Locale.getDefault().toString()}")
            appendLine("- Time Zone: ${TimeZone.getDefault().id}")

            val storage = getStorageInfo()
            if (storage != null) {
                appendLine("- Storage Free/Total: ${storage.first} / ${storage.second}")
            }

            val memory = getMemoryInfo(context)
            if (memory != null) {
                appendLine("- Memory Free/Total: ${memory.first} / ${memory.second}")
            }

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
            appendLine("- Timestamp: ${formatter.format(Date())}")
        }
        return info
    }

    private fun getStorageInfo(): Pair<String, String>? {
        return try {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            formatBytes(freeBytes) to formatBytes(totalBytes)
        } catch (e: Exception) {
            null
        }
    }

    private fun getMemoryInfo(context: Context): Pair<String, String>? {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val free = memInfo.availMem
            val total = memInfo.totalMem
            formatBytes(free) to formatBytes(total)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return "%.1f GB".format(gb)
    }
}
