package commands

import chatcontainer.ChatContainer
import org.example.Command

internal class SwitchChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a chat ID\n"
        }

        return switchChat(chatContainer, args)
    }

    private fun switchChat(container: ChatContainer, chatId: String): String {
        val fullChatId = container.findChatByPartialId( chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        container.currentChatId = fullChatId
        val currentChat = container.chats[fullChatId]
        val message = "Switched to chat: ${currentChat?.name}\n"

        return message
    }
}
