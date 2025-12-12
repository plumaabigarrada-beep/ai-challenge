package commands

import chatcontainer.ChatContainer
import org.example.Command

internal class ListChatsCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val chats = chatContainer.getChats()
        val currentChatId = chatContainer.getCurrentChatId()

        return buildString {
            appendLine("Available chats:")
            chats.forEach { (id, chat) ->
                val stats = chat.getStats()
                val current = if (id == currentChatId) " (current)" else ""
                appendLine("- [${id.take(8)}] ${chat.name}$current - ${stats.messageCount} messages, ${stats.totalTokens} tokens")
            }
            appendLine()
        }
    }
}
