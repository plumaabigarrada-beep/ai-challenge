package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.chatsaver.ChatSaver
import com.jamycake.aiagent.terminal.Command

internal class SaveChatCommand(
    private val chatContainer: ChatContainer,
    private val chatSaver: ChatSaver,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return if (args.isNullOrEmpty()) {
            chatSaver.saveChat(currentChat)
        } else {
            chatSaver.saveChat(currentChat, args)
        }
    }
}
