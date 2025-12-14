package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class CurrentChatCommand(
    private val allChats: () -> List<Chat>,
    private val focusManager: FocusManager,
    private val ui: UI
) : Command(listOf("--current-chat")) {

    override suspend fun execute(args: String?) {
        val currentChatId = focusManager.chatId

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
