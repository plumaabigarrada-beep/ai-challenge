package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class ListChatsCommand(
    private val allChats: () -> List<Chat>,
    private val ui: UI
) : Command(listOf("--chats")) {

    override suspend fun execute(args: String?) {
        val chats = allChats()

        if (chats.isEmpty()) {
            ui.out("No chats available")
            return
        }

        val output = buildString {
            appendLine("Chats:")
            appendLine("------")
            chats.forEach { chat ->
                appendLine("Chat ID: ${chat.id.value}")
            }
            appendLine("------")
            appendLine("Total: ${chats.size}")
        }

        ui.out(output)
    }
}