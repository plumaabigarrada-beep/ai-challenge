package commands

import chat.Chat

class DeleteChatCommand {
    fun execute(
        chats: MutableMap<String, Chat>,
        currentChatId: String,
        chatId: String?,
        findChatByPartialId: (String) -> String?
    ): Pair<String?, String> {
        if (chatId.isNullOrEmpty()) {
            return Pair(null, "Please provide a chat ID to delete\n")
        }

        if (chats.size == 1) {
            return Pair(null, "Cannot delete the last chat. Create a new one first.\n")
        }

        // Support partial ID matching
        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return Pair(null, "Chat not found: $chatId\n")
        }

        var newCurrentChatId = currentChatId
        if (fullChatId == currentChatId) {
            // Switch to another chat before deleting
            val anotherChatId = chats.keys.first { it != fullChatId }
            newCurrentChatId = anotherChatId
        }

        val deletedChat = chats.remove(fullChatId)
        val currentChat = chats[newCurrentChatId]
        val message = "Deleted chat: ${deletedChat?.name}. Switched to: ${currentChat?.name}\n"

        return Pair(newCurrentChatId, message)
    }
}
