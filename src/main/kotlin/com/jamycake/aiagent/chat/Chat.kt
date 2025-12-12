package com.jamycake.aiagent.chat

import com.jamycake.aiagent.context.Context
import java.util.*

/**
 * Chat data container.
 * Business logic is handled by command classes.
 */
internal class Chat(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Chat ${id.take(8)}",
    val config: Config,
    var context: Context,
) {

    internal val conversationHistory = mutableListOf<CoreMessage>()
    var lastAutoCompressNotification: String? = null

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
}

