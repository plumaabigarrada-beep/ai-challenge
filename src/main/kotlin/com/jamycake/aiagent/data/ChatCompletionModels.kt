package com.jamycake.aiagent.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Shared JSON parser configuration for all OpenAI-compatible clients
 */
val jsonParser = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

/**
 * Chat message in OpenAI format (role + content)
 */
@Serializable
data class ResponseChatMessage(
    val role: String,
    val content: String
)

/**
 * Chat completion request following OpenAI API format
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ResponseChatMessage>,
    val temperature: Double? = null,
    val stream: Boolean = false
)

/**
 * Single choice in chat completion response
 */
@Serializable
data class ChatChoice(
    val message: ResponseChatMessage,
    val index: Int? = null,
    val finish_reason: String? = null
)

/**
 * Token usage information
 */
@Serializable
data class ResponseChatUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)

/**
 * Chat completion response following OpenAI API format
 */
@Serializable
data class ChatCompletionResponse(
    val choices: List<ChatChoice>,
    val usage: ResponseChatUsage? = null,
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null
)
