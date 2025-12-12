package commands

import chat.Chat
import chatcontainer.ChatContainer
import org.example.Command
import org.example.CoreMessage
import org.example.context.Context

internal class ClearHistoryCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"
        return clearHistory(currentChat)
    }

    private fun clearHistory(chat: Chat): String {
        chat.conversationHistory.clear()
        chat.context = Context(messages = emptyList())
        if (chat.config.systemPrompt.isNotEmpty()) {
            chat.conversationHistory.add(CoreMessage(role = "system", content = chat.config.systemPrompt))
        }
        return "Conversation history cleared.\n"
    }
}
