package commands

import chat.Chat

class ClearHistoryCommand {
    fun execute(chat: Chat): String {
        return chat.clearHistory()
    }
}
