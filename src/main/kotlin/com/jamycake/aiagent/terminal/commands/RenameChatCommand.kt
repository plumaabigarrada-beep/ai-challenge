package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command

internal class RenameChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a new name for the chat\n"
        }

        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        currentChat.name = args
        return "Chat renamed to: $args\n"
    }
}
