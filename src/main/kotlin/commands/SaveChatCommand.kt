package commands

import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import org.example.Command

class SaveChatCommand(
    private val chatContainer: ChatContainer,
    private val chatSaver: ChatSaver,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return if (args.isNullOrEmpty()) {
            chatSaver.saveChat(currentChat)
        } else {
            chatSaver.saveChat(currentChat, args)
        }
    }
}
