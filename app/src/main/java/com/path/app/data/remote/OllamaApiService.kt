package com.path.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

data class OllamaResponse(
    val response: String,
    val done: Boolean
)

data class OllamaModelResponse(
    val models: List<OllamaModel>
)

data class OllamaModel(
    val name: String
)

interface OllamaApiService {
    @POST("api/generate")
    suspend fun generate(@Body request: OllamaRequest): OllamaResponse
    
    @retrofit2.http.GET("api/tags")
    suspend fun getTags(): OllamaModelResponse
}
