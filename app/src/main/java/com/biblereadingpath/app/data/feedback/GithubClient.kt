package com.biblereadingpath.app.data.feedback

import com.biblereadingpath.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GithubClient {
    private const val BASE_URL = "https://api.github.com/"

    val api: GithubApi by lazy { createRetrofit().create(GithubApi::class.java) }

    val hasValidConfig: Boolean
        get() = BuildConfig.GITHUB_API_TOKEN.isNotBlank() &&
                BuildConfig.GITHUB_REPO_OWNER.isNotBlank() &&
                BuildConfig.GITHUB_REPO_NAME.isNotBlank()

    val owner: String get() = BuildConfig.GITHUB_REPO_OWNER
    val repo: String get() = BuildConfig.GITHUB_REPO_NAME
    val missingConfigMessage: String
        get() {
            val missing = mutableListOf<String>()
            if (BuildConfig.GITHUB_API_TOKEN.isBlank()) missing.add("GitHub API token")
            if (BuildConfig.GITHUB_REPO_OWNER.isBlank()) missing.add("repo owner")
            if (BuildConfig.GITHUB_REPO_NAME.isBlank()) missing.add("repo name")
            return if (missing.isEmpty()) ""
            else "Missing GitHub config: ${missing.joinToString(", ")}. Set them in local.properties."
        }

    private fun createRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .addHeader("User-Agent", "Path-Android/1.1.0")
                if (BuildConfig.GITHUB_API_TOKEN.isNotBlank()) {
                    request.addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}")
                }
                chain.proceed(request.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
