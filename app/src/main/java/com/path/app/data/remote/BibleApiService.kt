package com.path.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

data class ApiVerse(
    @SerializedName("book_id") val bookId: String,
    @SerializedName("book_name") val bookName: String,
    @SerializedName("chapter") val chapter: Int,
    @SerializedName("verse") val verse: Int,
    @SerializedName("text") val text: String
)

data class BibleApiResponse(
    @SerializedName("reference") val reference: String,
    @SerializedName("verses") val verses: List<ApiVerse>,
    @SerializedName("text") val text: String,
    @SerializedName("translation_id") val translationId: String,
    @SerializedName("translation_name") val translationName: String,
    @SerializedName("translation_note") val translationNote: String
)

interface BibleApiService {
    @GET("{reference}?translation=web")
    suspend fun getChapter(@Path("reference") reference: String): BibleApiResponse
}
