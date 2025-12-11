package commands

import chat.Chat

class SendMessageCommand {
    suspend fun execute(chat: Chat, text: String): String {
        return chat.sendMessage(text)
    }
}
