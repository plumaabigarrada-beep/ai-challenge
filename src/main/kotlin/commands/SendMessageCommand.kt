package commands

import chat.Chat
import chat.ChatStats
import chat.ChatUsage
import chatcontainer.ChatContainer
import client.Client
import compressor.ContextCompressor
import org.example.ClientType
import org.example.Command
import org.example.ContextWindowConfig
import org.example.CoreMessage
import org.example.context.Context
import org.example.context.ContextMessage
import org.example.contextsender.ContextSender

internal class SendMessageCommand(
    private val chatContainer: ChatContainer,
    private val contextCompressor: ContextCompressor,
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
        val contextMessages = buildContextMessages(chat)
        val context = Context(messages = contextMessages)

        // Get the appropriate client and create ContextSender
        val client = getClient(chat)
        val contextSender = ContextSender(client)

        // Measure response time
        val startTime = System.currentTimeMillis()

        // Send context via ContextSender and receive response
        val (responseContext, chatMessage) = contextSender.sendContext(
            context = context,
            temperature = chat.config.temperature,
            model = chat.config.model
        )

        val duration = System.currentTimeMillis() - startTime

        // Update chat context with received context
        chat.context = responseContext

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

    private fun buildContextMessages(chat: Chat): List<ContextMessage> {
        val messages = mutableListOf<ContextMessage>()

        // Add system prompt if present
        if (chat.config.systemPrompt.isNotEmpty()) {
            messages.add(ContextMessage(role = "system", content = chat.config.systemPrompt))
        }

        // Add conversation history
        chat.conversationHistory.forEach { coreMessage ->
            messages.add(ContextMessage(role = coreMessage.role, content = coreMessage.content))
        }

        return messages
    }

    private suspend fun checkAndAutoCompress(chat: Chat): String? {
        if (!chat.config.autoCompressEnabled) {
            return null
        }

        val stats = getStats(chat)
        val contextWindow = ContextWindowConfig.getContextWindow(chat.config.model)
        val threshold = (contextWindow * chat.config.autoCompressThreshold).toInt()

        if (stats.totalTokens >= threshold) {
            // Auto-compress the chat context
            val (compressedContext, usage) = contextCompressor.compress(chat.context)

            // Update chat with compressed context
            chat.context = compressedContext

            // Clear conversation history and add compression summary
            chat.conversationHistory.clear()
            if (compressedContext.messages.isNotEmpty()) {
                val summaryMessage = compressedContext.messages.first()
                chat.conversationHistory.add(
                    CoreMessage(
                        role = summaryMessage.role,
                        content = summaryMessage.content,
                        tokens = usage?.total_tokens
                    )
                )
            }

            return if (chat.config.autoCompressNotify) {
                val usageInfo = if (usage != null) {
                    " (${usage.total_tokens ?: 0} tokens used)"
                } else ""
                "[Auto-compressed: ${stats.totalTokens} tokens exceeded ${(chat.config.autoCompressThreshold * 100).toInt()}% of $contextWindow token limit$usageInfo]"
            } else {
                null
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

    private fun getClient(chat: Chat): Client = when (chat.config.clientType) {
        ClientType.PERPLEXITY -> chat.clients[ClientType.PERPLEXITY]!!
        ClientType.HUGGINGFACE -> chat.clients[ClientType.HUGGINGFACE]!!
        ClientType.LMSTUDIO -> chat.clients[ClientType.LMSTUDIO]!!
    }
}
