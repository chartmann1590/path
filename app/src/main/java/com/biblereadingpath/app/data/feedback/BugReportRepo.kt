package com.biblereadingpath.app.data.feedback

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.feedbackDataStore by preferencesDataStore(name = "feedback_bug_reports")

class BugReportRepo(private val context: Context) {
    private val KEY_BUG_REPORTS = stringPreferencesKey("bug_reports_list")
    private val gson = Gson()

    val bugReports: Flow<List<BugReport>> = context.feedbackDataStore.data.map { prefs ->
        val json = prefs[KEY_BUG_REPORTS] ?: "[]"
        try {
            val type = object : TypeToken<List<BugReport>>() {}.type
            val list: List<BugReport> = gson.fromJson(json, type)
            list.sortedByDescending { it.number }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveBugReport(report: BugReport) {
        context.feedbackDataStore.edit { prefs ->
            val json = prefs[KEY_BUG_REPORTS] ?: "[]"
            val type = object : TypeToken<MutableList<BugReport>>() {}.type
            val list: MutableList<BugReport> = try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                mutableListOf()
            }
            val index = list.indexOfFirst { it.number == report.number }
            if (index >= 0) {
                list[index] = report
            } else {
                list.add(report)
            }
            prefs[KEY_BUG_REPORTS] = gson.toJson(list)
        }
    }

    suspend fun updateBugReports(reports: List<BugReport>) {
        context.feedbackDataStore.edit { prefs ->
            prefs[KEY_BUG_REPORTS] = gson.toJson(reports)
        }
    }

    suspend fun getBugReportsList(): List<BugReport> {
        val prefs = context.feedbackDataStore.data.first()
        val json = prefs[KEY_BUG_REPORTS] ?: "[]"
        return try {
            val type = object : TypeToken<List<BugReport>>() {}.type
            val list: List<BugReport> = gson.fromJson(json, type)
            list.sortedByDescending { it.number }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
