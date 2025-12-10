package client

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
data class ChatMessage(
    val role: String,
    val content: String
)

/**
 * Chat completion request following OpenAI API format
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    val stream: Boolean = false
)

/**
 * Single choice in chat completion response
 */
@Serializable
data class ChatChoice(
    val message: ChatMessage,
    val index: Int? = null,
    val finish_reason: String? = null
)

/**
 * Token usage information
 */
@Serializable
data class ChatUsage(
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
    val usage: ChatUsage? = null,
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null
)
