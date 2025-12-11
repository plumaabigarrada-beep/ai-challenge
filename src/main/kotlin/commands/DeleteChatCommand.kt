package commands

import chatcontainer.ChatContainer
import org.example.Command

class DeleteChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a chat ID to delete\n"
        }

        return chatContainer.deleteChat(args)
    }
}
