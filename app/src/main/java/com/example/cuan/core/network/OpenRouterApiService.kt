package com.example.cuan.core.network

import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class OpenRouterRequest(
    val model: String = "nvidia/nemotron-3-super-120b-a12b:free",
    val messages: List<ChatMessage>
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<ChatChoice>
)

@Serializable
data class ChatChoice(
    val message: ChatChoiceMessage
)

@Serializable
data class ChatChoiceMessage(
    val content: String
)

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorizationHeader: String,
        @Body request: okhttp3.RequestBody
    ): ResponseBody
}
