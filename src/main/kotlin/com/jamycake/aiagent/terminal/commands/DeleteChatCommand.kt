package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command

internal class DeleteChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a chat ID to delete\n"
        }

        return deleteChat(chatContainer, args)
    }

    private fun deleteChat(container: ChatContainer, chatId: String): String {
        if (container.chats.size == 1) {
            return "Cannot delete the last chat. Create a new one first.\n"
        }

        val fullChatId = container.findChatByPartialId( chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        var newCurrentChatId = container.currentChatId
        if (fullChatId == container.currentChatId) {
            val anotherChatId = container.chats.keys.first { it != fullChatId }
            newCurrentChatId = anotherChatId
            container.currentChatId = newCurrentChatId
        }

        val deletedChat = container.chats.remove(fullChatId)
        val currentChat = container.chats[newCurrentChatId]
        val message = "Deleted chat: ${deletedChat?.name}. Switched to: ${currentChat?.name}\n"

        return message
    }
}
