package commands

import chat.Chat

class RenameChatCommand {
    fun execute(currentChat: Chat, name: String?): String {
        if (name.isNullOrEmpty()) {
            return "Please provide a new name for the chat\n"
        }
        currentChat.name = name
        return "Chat renamed to: $name\n"
    }
}
