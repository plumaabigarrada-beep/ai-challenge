package commands

import chatcontainer.ChatContainer
import org.example.Command

class SendMessageCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a message\n"
        }

        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return currentChat.sendMessage(args)
    }
}
