package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal class SendMessageCommand(
    private val getCurrentUser: () -> User?,
    private val allChats: () -> List<Chat>,
    private val space: Space,
    private val chats: Chats,
    private val agents: Agents
) : Command(values = listOf("--send")) {

    override suspend fun execute(args: String?) {
        if (args.isNullOrEmpty()) return

        val user = getCurrentUser()
        if (user == null) return

        user.sendMessage(args)

        // Save the current chat
        val currentChatId = user.chatId
        if (currentChatId != null && currentChatId.value.isNotEmpty()) {
            val chat = allChats().find { it.id.value == currentChatId.value }
            if (chat != null) {
                chats.saveChat(chat)
            }
        }

        // Save all agents that are wired to this chat
        space.allAgents.forEach { agent ->
            if (agent.state.chatId == currentChatId) {
                agents.save(agent)
            }
        }
    }
}