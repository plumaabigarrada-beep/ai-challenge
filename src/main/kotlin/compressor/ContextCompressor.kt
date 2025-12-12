package compressor

import chat.ChatUsage
import org.example.ClientType
import org.example.context.Context
import org.example.context.ContextMessage
import org.example.contextsender.ContextSender

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
            val (responseContext, chatMessage) = contextSender.sendContext(
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
        if (compressedSummary.isNullOrEmpty()) {
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