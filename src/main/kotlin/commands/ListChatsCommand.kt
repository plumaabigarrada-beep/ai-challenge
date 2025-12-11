package commands

import chat.Chat

class ListChatsCommand {
    fun execute(chats: Map<String, Chat>, currentChatId: String): String {
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
