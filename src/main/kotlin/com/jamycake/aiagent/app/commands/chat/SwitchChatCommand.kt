package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal class SwitchChatCommand(
    private val allChats: () -> List<Chat>,
    private val getCurrentUser: () -> User?,
    private val space: Space,
    private val ui: UI
) : Command(listOf("--switch-chat")) {

    override suspend fun execute(args: String?) {
        if (args.isNullOrBlank()) {
            ui.out("Usage: --switch-chat [chat-id]")
            return
        }

        val chatId = args.trim()

        // Check if chat exists
        val chat = allChats().find { it.id.value == chatId }

        if (chat == null) {
            ui.out("Chat not found: $chatId")
            return
        }

        // Get current user
        val user = getCurrentUser()
        if (user == null) {
            ui.out("No user available")
            return
        }

        // Switch user's chat
        user.chatId = ChatId(chatId)

        // Wire the current user to the new chat so they receive messages
        space.wireUserToChat(user.id, ChatId(chatId))

        ui.out("Switched to chat: $chatId")
    }
}
