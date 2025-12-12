package commands

import chatcontainer.ChatContainer
import org.example.Command

internal class GetContextCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        val context = currentChat.context
        val messageCount = context.messages.size

        return buildString {
            appendLine("Current Context Information:")
            appendLine("- Messages in context: $messageCount")
            appendLine()
        }
    }
}
