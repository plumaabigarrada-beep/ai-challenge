package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class CurrentChatCommand(
    private val allChats: () -> List<Chat>,
    private val getCurrentUser: () -> User?,
    private val ui: UI
) : Command(listOf("--current-chat")) {

    override suspend fun execute(args: String?) {
        // Get current user
        val user = getCurrentUser()
        if (user == null) {
            ui.out("No user available")
            return
        }

        val currentChatId = user.chatId

        if (currentChatId.value.isEmpty()) {
            ui.out("No chat is currently focused")
            return
        }

        // Find the chat
        val chat = allChats().find { it.id.value == currentChatId.value }

        if (chat == null) {
            ui.out("Current chat not found: ${currentChatId.value}")
            ui.out("(The focused chat may have been deleted)")
            return
        }

        val output = buildString {
            appendLine("Current Chat:")
            appendLine("-------------")
            appendLine("Chat ID: ${chat.id.value}")
            appendLine("Messages: ${chat.messages.size}")
            appendLine("-------------")
        }

        ui.out(output)
    }
}
