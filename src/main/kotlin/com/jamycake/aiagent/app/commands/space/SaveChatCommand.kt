package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class SaveChatCommand(
    private val chats: Chats,
    private val allChats: () -> List<Chat>,
    private val ui: UI
) : Command(listOf("--save-chat")) {

    override suspend fun execute(args: String?) {
        val chatList = allChats()

        if (chatList.isEmpty()) {
            ui.out("No chats to save")
            return
        }

        chatList.forEach { chat ->
            chats.saveChat(chat)
        }

        ui.out("Saved ${chatList.size} chat(s) successfully")
    }
}
