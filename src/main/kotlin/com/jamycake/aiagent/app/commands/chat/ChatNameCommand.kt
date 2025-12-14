package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class ChatNameCommand(
    private val allChats: () -> List<Chat>,
    private val getCurrentUser: () -> User?,
    private val ui: UI
) : Command(listOf("--chat-name")) {

    override suspend fun execute(args: String?) {
        val user = getCurrentUser()
        if (user == null) {
            ui.out("No user available")
            return
        }

        val currentChatId = user.chatId
        if (currentChatId?.value?.isEmpty() == true) {
            ui.out("No chat is currently focused")
            return
        }

        val chat = allChats().find { it.id.value == currentChatId?.value }
        if (chat == null) {
            ui.out("Current chat not found: ${currentChatId?.value}")
            return
        }

        if (args.isNullOrEmpty()) {
            // Show current chat name
            if (chat.name.isEmpty()) {
                ui.out("Current chat has no name set")
            } else {
                ui.out("Current chat name: ${chat.name}")
            }
        } else {
            // Set chat name
            chat.name = args
            ui.out("Chat name set to: ${args}")
        }
    }
}
