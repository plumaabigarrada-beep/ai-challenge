package commands

import chatcontainer.ChatContainer
import org.example.Command

class ClearHistoryCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"
        return currentChat.clearHistory()
    }
}
