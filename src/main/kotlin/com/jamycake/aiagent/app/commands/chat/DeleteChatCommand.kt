package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class DeleteChatCommand(
    private val allChats: () -> List<Chat>,
    private val getCurrentUser: () -> User?,
    private val space: Space,
    private val chats: Chats,
    private val agents: Agents,
    private val ui: UI
) : Command(listOf("--delete-chat")) {

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

        val chatName = if (chat.name.isEmpty()) chat.id.value else chat.name

        // Remove chat from space (unwire all members)
        space.removeChat(chat.id)

        // Delete chat file
        chats.deleteChat(chat)

        // Save all agents that were in this chat (their chatId is now null)
        space.allAgents.forEach { agent ->
            if (agent.state.chatId == null || agent.state.chatId?.value?.isEmpty() == true) {
                agents.save(agent)
            }
        }

        ui.out("Chat deleted: $chatName")
    }
}
