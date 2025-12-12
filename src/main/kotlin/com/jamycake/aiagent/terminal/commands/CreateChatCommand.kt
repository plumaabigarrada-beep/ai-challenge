package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chat.Chat
import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.context.Context

internal class CreateChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val newChat = newChat(chatContainer, args)
        return "Created and switched to new chat: ${newChat.name}\n"
    }

    private fun newChat(container: ChatContainer, name: String?): Chat {
        val newChat = Chat(
            name = name ?: "Chat ${container.chats.size + 1}",
            clients = container.clients,
            config = container.defaultConfig.copy(),
            context = Context(messages = emptyList()),
        )

        container.currentChatId = newChat.id
        container.chats[newChat.id] = newChat

        return newChat
    }
}
