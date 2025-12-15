package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class CreateChatCommand(
    private val chats: Chats,
    private val space: Space,
    private val ui: UI
) : Command(listOf("--create-chat")) {

    override suspend fun execute(args: String?) {
        val newChat = chats.newChat()
        space.addChat(newChat)

        ui.out("Chat created successfully!")
        ui.out("Chat ID: ${newChat.id.value}")
    }
}
