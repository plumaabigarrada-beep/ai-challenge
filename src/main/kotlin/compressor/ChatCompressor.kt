package compressor

import chat.Chat
import client.Client
import kotlinx.coroutines.runBlocking
import org.example.CoreMessage

class ChatCompressor(
    private val client: Client,
    private val compressPrompt: String,
) {

    suspend fun compress(chat: Chat): Chat {
        // Get the current conversation history
        val currentHistory = chat.getConversationHistory()

        // If history is empty or too short, no need to compress
        if (currentHistory.size <= 2) {
            return chat
        }

        // Format the conversation history as a string
        val historyText = chat.formatHistoryAsString()

        // Create the compression request
        val compressionRequest = listOf(
            CoreMessage(
                role = "system",
                content = compressPrompt
            ),
            CoreMessage(
                role = "user",
                content = historyText
            )
        )

        // Send to AI to compress
        val compressedSummary =  try {
            val response = client.sendMessage(
                conversationHistory = compressionRequest,
                temperature = 0.3, // Lower temperature for more consistent compression
                model = client.models().first() // Use the first available model
            )
            response.content
        } catch (e: Exception) {
            // If compression fails, return original chat
            null
        }

        // If compression failed, return original chat
        if (compressedSummary.isNullOrEmpty()) {
            return chat
        }

        // Replace conversation history with compressed version
        val compressedHistory = listOf(
            CoreMessage(
                role = "assistant",
                content = "Previous conversation summary:\n\n$compressedSummary"
            )
        )

        chat.setConversationHistory(compressedHistory)

        return chat
    }
}