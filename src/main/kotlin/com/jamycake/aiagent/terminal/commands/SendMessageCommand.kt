package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chat.Chat
import com.jamycake.aiagent.chat.ChatStats
import com.jamycake.aiagent.chat.ChatUsage
import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.ContextWindowConfig
import com.jamycake.aiagent.chat.CoreMessage
import com.jamycake.aiagent.context.ContextMessage
import com.jamycake.aiagent.contextsender.ContextSender

internal class SendMessageCommand(
    private val chatContainer: ChatContainer,
    private val contextSender: ContextSender,
    private val compressContextCommand: com.jamycake.aiagent.terminal.commands.CompressContextCommand,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a message\n"
        }

        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return sendMessage(currentChat, args)
    }

    private suspend fun sendMessage(chat: Chat, text: String): String {
        // Check if auto-compression is needed before sending
        val compressionNotice = checkAndAutoCompress(chat)

        // Clear notification after being read
        chat.lastAutoCompressNotification = null

        // Add user message to chat history
        chat.conversationHistory.add(CoreMessage(role = "user", content = text))

        // Build Context from conversation history
        val newContext = chat.context.copy(
            messages = chat.context.messages + ContextMessage(role = "user", content = text),
        )

        chat.context = newContext

        // Measure response time
        val startTime = System.currentTimeMillis()

        // Send context via ContextSender and receive response
        val chatMessage = contextSender.sendContext(
            context = newContext,
            temperature = chat.config.temperature,
            model = chat.config.model,
            clientType = chat.config.clientType,
        )

        val duration = System.currentTimeMillis() - startTime

        // Update chat context with received context
        chat.context = chat.context.copy(
            messages = chat.context.messages + ContextMessage(role = chatMessage.role, content = chatMessage.message, chatMessage.usage),
        )


        if (chatMessage.message.isNotEmpty()) {
            // Update the last user message with prompt tokens from usage
            if (chatMessage.usage?.prompt_tokens != null && chat.conversationHistory.isNotEmpty()) {
                val lastIndex = chat.conversationHistory.lastIndex
                chat.conversationHistory[lastIndex] = chat.conversationHistory[lastIndex].copy(
                    tokens = chatMessage.usage.prompt_tokens
                )
            }

            // Add assistant response with completion tokens and duration
            chat.conversationHistory.add(
                CoreMessage(
                    role = chatMessage.role,
                    content = chatMessage.message,
                    tokens = chatMessage.usage?.completion_tokens,
                    durationMs = duration
                )
            )

            // Build response with optional token and time information
            val responseText = if (chat.config.showTokens) {
                formatResponseWithTokens(chatMessage.usage, chatMessage.message, duration)
            } else {
                chatMessage.message
            }

            // Prepend compression notice if auto-compressed
            return if (compressionNotice != null) {
                "$compressionNotice\n$responseText"
            } else {
                responseText
            }
        } else {
            chat.conversationHistory.removeLastOrNull()
            return ""
        }
    }

    private suspend fun checkAndAutoCompress(chat: Chat): String? {
        if (!chat.config.autoCompressEnabled) {
            return null
        }

        val stats = getStats(chat)
        val contextWindow = ContextWindowConfig.getContextWindow(chat.config.model)
        val threshold = (contextWindow * chat.config.autoCompressThreshold).toInt()

        if (stats.totalTokens >= threshold) {

            val message = compressContextCommand.execute(null)

            if (chat.config.autoCompressNotify) {
                return message
            }
        }

        return null
    }

    private fun getStats(chat: Chat): ChatStats {
        return chat.getStats()
    }

    private fun formatResponseWithTokens(usage: ChatUsage?, message: String, duration: Long): String = buildString {
        append(message)
        append("\n\n[")

        // Add token information if available
        if (usage != null && (usage.prompt_tokens != null || usage.completion_tokens != null)) {
            append("Tokens: ")
            if (usage.prompt_tokens != null) append("prompt=${usage.prompt_tokens}")
            if (usage.prompt_tokens != null && usage.completion_tokens != null) append(", ")
            if (usage.completion_tokens != null) append("response=${usage.completion_tokens}")
            if (usage.total_tokens != null) {
                append(", total=${usage.total_tokens}")
            } else {
                val total = (usage.prompt_tokens ?: 0) + (usage.completion_tokens ?: 0)
                append(", total=$total")
            }
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
}
