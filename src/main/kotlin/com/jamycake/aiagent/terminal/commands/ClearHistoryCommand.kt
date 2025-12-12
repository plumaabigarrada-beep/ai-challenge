package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.chat.Chat
import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.CoreMessage
import org.example.context.Context

internal class ClearHistoryCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"
        return clearHistory(currentChat)
    }

    private fun clearHistory(chat: Chat): String {
        chat.conversationHistory.clear()
        chat.context = Context(messages = emptyList())
        if (chat.config.systemPrompt.isNotEmpty()) {
            chat.conversationHistory.add(CoreMessage(role = "system", content = chat.config.systemPrompt))
        }
        return "Conversation history cleared.\n"
    }
}
