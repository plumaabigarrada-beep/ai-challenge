package commands

import chat.Chat

class SwitchChatCommand {
    fun execute(
        chats: Map<String, Chat>,
        chatId: String?,
        findChatByPartialId: (String) -> String?
    ): Pair<String?, String> {
        if (chatId.isNullOrEmpty()) {
            return Pair(null, "Please provide a chat ID\n")
        }

        // Support partial ID matching
        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return Pair(null, "Chat not found: $chatId\n")
        }

        val currentChat = chats[fullChatId]
        val message = "Switched to chat: ${currentChat?.name}\n"

        return Pair(fullChatId, message)
    }
}
