package commands

import chatcontainer.ChatContainer
import org.example.Command
import org.example.Config
import org.example.ContextWindowConfig

class GetConfigCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        val stats = currentChat.getStats()
        val config = currentChat.config

        val contextWindow = ContextWindowConfig.getContextWindow(config.model)
        val threshold = (contextWindow * config.autoCompressThreshold).toInt()

        return buildString {
            appendLine("Current Configuration:")
            appendLine("- Client: ${config.clientType.name.lowercase()}")
            appendLine("- Model: ${config.model}")
            appendLine("- Temperature: ${config.temperature}")
            appendLine("- System Prompt: ${config.systemPrompt.ifEmpty { "(not set)" }}")
            appendLine("- Show Tokens: ${if (config.showTokens) "enabled" else "disabled"}")
            appendLine("- Auto-Compress: ${if (config.autoCompressEnabled) "enabled" else "disabled"}")
            if (config.autoCompressEnabled) {
                appendLine("- Auto-Compress Threshold: ${(config.autoCompressThreshold * 100).toInt()}% ($threshold/$contextWindow tokens)")
            }
            appendLine("- Context Window: $contextWindow tokens")
            appendLine("- Current Chat: ${currentChat.name}")
            appendLine("- Conversation History: ${stats.messageCount} messages")
            appendLine("- Total Tokens Used: ${stats.totalTokens}")
            if (stats.avgResponseTime > 0) {
                appendLine("- Average Response Time: ${String.format("%.2fs", stats.avgResponseTime / 1000.0)}")
                appendLine("- Total Response Time: ${String.format("%.2fs", stats.totalResponseTime / 1000.0)}")
            }
            appendLine()
        }
    }
}
