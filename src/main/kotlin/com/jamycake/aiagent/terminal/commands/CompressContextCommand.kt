package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.compressor.ContextCompressor
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.CoreMessage

internal class CompressContextCommand(
    private val chatContainer: ChatContainer,
    private val contextCompressor: ContextCompressor,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return try {
            // Compress the chat context
            val (compressedContext, usage) = contextCompressor.compress(
                context = currentChat.context,
                clientType = currentChat.config.clientType,
                model = currentChat.config.model
            )

            // Update chat with compressed context
            currentChat.context = compressedContext

            if (compressedContext.messages.isNotEmpty()) {
                val summaryMessage = compressedContext.messages.first()
                currentChat.conversationHistory.add(
                    CoreMessage(
                        role = summaryMessage.role,
                        content = summaryMessage.content,
                        tokens = usage?.total_tokens
                    )
                )
            }

            val usageInfo = if (usage != null) {
                " Tokens used: ${usage.total_tokens ?: 0}"
            } else ""
            "Chat history compressed successfully.$usageInfo\n"
        } catch (e: Exception) {
            "Error compressing chat: ${e.message}\n"
        }
    }
}
