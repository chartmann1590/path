package com.biblereadingpath.app.data.feedback

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Talks to the cloudflare-worker/ feedback relay, not api.github.com directly. See
 * cloudflare-worker/src/index.ts, which holds the GitHub token server-side as a Worker
 * secret. Previously this embedded BuildConfig.GITHUB_API_TOKEN client-side as a Bearer
 * header, which shipped a real repo-write PAT in every release build (extractable from
 * the APK).
 */
object GithubClient {
    private const val BASE_URL = "https://path-github-feedback.charles-h-hartmann1.workers.dev/"

    val api: GithubApi by lazy { createRetrofit().create(GithubApi::class.java) }

    val hasValidConfig: Boolean = true
    val missingConfigMessage: String = ""

    private fun createRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
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
