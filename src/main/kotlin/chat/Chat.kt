package chat

import chatsaver.ChatSaver
import client.Client
import client.CoreClientResponse
import compressor.ChatCompressor
import org.example.ClientType
import org.example.Config
import org.example.ContextWindowConfig
import org.example.CoreMessage
import java.util.UUID

class Chat(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Chat ${id.take(8)}",
    private val clients: Map<ClientType, Client>,
    val config: Config,
    private val chatCompressor: ChatCompressor,
    private val saver: ChatSaver,
) {

    private val conversationHistory = mutableListOf<CoreMessage>()
    var lastAutoCompressNotification: String? = null

    suspend fun sendMessage(text: String): String {
        // Check if auto-compression is needed before sending
        val compressionNotice = checkAndAutoCompress()

        // Clear notification after being read
        lastAutoCompressNotification = null
        conversationHistory.add(CoreMessage(role = "user", content = text))

        val history = if (config.systemPrompt.isEmpty()) {
            conversationHistory
        } else {
            listOf(CoreMessage(role = "system", content = config.systemPrompt)) + conversationHistory
        }

        val client = getClient()

        // Measure response time
        val startTime = System.currentTimeMillis()
        val response = client.sendMessage(
            conversationHistory = history,
            temperature = config.temperature,
            model = config.model
        )
        val duration = System.currentTimeMillis() - startTime

        if (response.content.isNotEmpty()) {
            // Update the last user message with prompt tokens
            if (response.promptTokens != null && conversationHistory.isNotEmpty()) {
                val lastIndex = conversationHistory.lastIndex
                conversationHistory[lastIndex] = conversationHistory[lastIndex].copy(
                    tokens = response.promptTokens
                )
            }

            // Add assistant response with response tokens and duration
            conversationHistory.add(
                CoreMessage(
                    role = "assistant",
                    content = response.content,
                    tokens = response.responseTokens,
                    durationMs = duration
                )
            )

            // Build response with optional token and time information
            val responseText = if (config.showTokens) {
                formatResponseWithTokens(response, duration)
            } else {
                response.content
            }

            // Prepend compression notice if auto-compressed
            return if (compressionNotice != null) {
                "$compressionNotice\n$responseText"
            } else {
                responseText
            }
        } else {
            conversationHistory.removeLastOrNull()
            return ""
        }
    }

    fun clearHistory(): String {
        conversationHistory.clear()
        if (config.systemPrompt.isNotEmpty()) {
            conversationHistory.add(CoreMessage(role = "system", content = config.systemPrompt))
        }
        return "Conversation history cleared.\n"
    }

    fun getStats(): ChatStats {
        val totalTokens = conversationHistory.sumOf { it.tokens ?: 0 }
        val responsesWithDuration = conversationHistory.filter { it.role == "assistant" && it.durationMs != null }
        val totalDuration = responsesWithDuration.sumOf { it.durationMs ?: 0 }
        val avgDuration = if (responsesWithDuration.isNotEmpty()) {
            totalDuration / responsesWithDuration.size
        } else 0

        return ChatStats(
            messageCount = conversationHistory.size,
            totalTokens = totalTokens,
            avgResponseTime = avgDuration,
            totalResponseTime = totalDuration
        )
    }

    fun getConversationHistory(): List<CoreMessage> {
        return conversationHistory.toList()
    }

    fun setConversationHistory(history: List<CoreMessage>) {
        conversationHistory.clear()
        conversationHistory.addAll(history)
    }

    fun formatHistoryAsString(): String = buildString {
        conversationHistory.forEach { message ->
            appendLine("${message.role.uppercase()}:")
            appendLine(message.content)
            appendLine()
        }
    }

    private suspend fun checkAndAutoCompress(): String? {
        if (!config.autoCompressEnabled || chatCompressor == null) {
            return null
        }

        val stats = getStats()
        val contextWindow = ContextWindowConfig.getContextWindow(config.model)
        val threshold = (contextWindow * config.autoCompressThreshold).toInt()

        if (stats.totalTokens >= threshold) {
            // Auto-compress the chat
            chatCompressor.compress(this)

            return if (config.autoCompressNotify) {
                "[Auto-compressed: ${stats.totalTokens} tokens exceeded ${(config.autoCompressThreshold * 100).toInt()}% of $contextWindow token limit]"
            } else {
                null
            }
        }

        return null
    }

    private fun formatResponseWithTokens(response: CoreClientResponse, duration: Long): String = buildString {
        append(response.content)
        append("\n\n[")

        // Add token information if available
        if (response.promptTokens != null || response.responseTokens != null) {
            append("Tokens: ")
            if (response.promptTokens != null) append("prompt=${response.promptTokens}")
            if (response.promptTokens != null && response.responseTokens != null) append(", ")
            if (response.responseTokens != null) append("response=${response.responseTokens}")
            val total = (response.promptTokens ?: 0) + (response.responseTokens ?: 0)
            append(", total=$total")
            append(" | ")
        }

        // Add time information
        append("Time: ")
        if (duration < 1000) {
            append("${duration}ms")
        } else {
            val seconds = duration / 1000.0
            append(String.format("%.2fs", seconds))
        }
        append("]")
    }

    private fun getClient(): Client = when (config.clientType) {
        ClientType.PERPLEXITY -> clients[ClientType.PERPLEXITY]!!
        ClientType.HUGGINGFACE -> clients[ClientType.HUGGINGFACE]!!
        ClientType.LMSTUDIO -> clients[ClientType.LMSTUDIO]!!
    }
}

data class ChatStats(
    val messageCount: Int,
    val totalTokens: Int,
    val avgResponseTime: Long,
    val totalResponseTime: Long
)