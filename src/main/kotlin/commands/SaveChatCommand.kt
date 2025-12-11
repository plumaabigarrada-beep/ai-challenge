package commands

import chat.Chat
import chatsaver.ChatSaver

class SaveChatCommand {
    fun execute(chatSaver: ChatSaver, currentChat: Chat, directory: String? = null): String {
        return if (directory.isNullOrEmpty()) {
            chatSaver.saveChat(currentChat)
        } else {
            chatSaver.saveChat(currentChat, directory)
        }
    }
}
