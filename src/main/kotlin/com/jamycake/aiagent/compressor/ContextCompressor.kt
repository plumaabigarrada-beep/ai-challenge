package com.jamycake.aiagent.compressor

import com.jamycake.aiagent.chat.ChatUsage
import com.jamycake.aiagent.context.Context
import com.jamycake.aiagent.context.ContextMessage
import com.jamycake.aiagent.contextsender.ContextSender
import com.jamycake.aiagent.chat.ClientType

internal class ContextCompressor(
    private val contextSender: ContextSender,
    private val compressPrompt: String,
    private val defaultClientType: ClientType,
    private val defaultModel: String,
) {

    suspend fun compress(
        context: Context,
        clientType: ClientType = defaultClientType,
        model: String = defaultModel
    ): Pair<Context, ChatUsage?> {
        // Get the current conversation history
        val currentHistory = context.messages

        // If history is empty or too short, no need to compress
        if (currentHistory.size <= 2) {
            return Pair(context, null)
        }

        // Format the conversation history as a string
        val historyText = context.formatHistoryAsString()

        // Create the compression request as Context
        val compressionContext = Context(
            messages = listOf(
                ContextMessage(
                    role = "system",
                    content = compressPrompt
                ),
                ContextMessage(
                    role = "user",
                    content = historyText
                )
            )
        )

        // Send to AI to compress via ContextSender
        val (compressedSummary, usage) = try {
            val chatMessage = contextSender.sendContext(
                context = compressionContext,
                temperature = 0.3, // Lower temperature for more consistent compression
                model = model,
                clientType = clientType
            )
            Pair(chatMessage.message, chatMessage.usage)
        } catch (e: Exception) {
            // If compression fails, return original context
            return Pair(context, null)
        }

        // If compression failed, return original context
        if (compressedSummary.isEmpty()) {
            return Pair(context, null)
        }

        // Replace conversation history with compressed version
        val compressedContext = Context(
            messages = listOf(
                ContextMessage(
                    role = "assistant",
                    content = "Previous conversation summary:\n\n$compressedSummary"
                )
            )
        )

        return Pair(compressedContext, usage)
    }
}