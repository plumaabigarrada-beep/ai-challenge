package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class AgentsStatsCommand(
    private val getCurrentUser: () -> User?,
    private val space: Space,
    private val ui: UI
) : Command(listOf("--agents-stats")) {

    override suspend fun execute(args: String?) {
        val user = getCurrentUser()
        if (user == null) {
            ui.out("No user available")
            return
        }

        val currentChatId = user.chatId
        if (currentChatId?.value?.isEmpty() == true || currentChatId == null) {
            ui.out("No chat is currently focused")
            return
        }

        // Get all agents wired to the current chat
        val agentsInChat = space.allAgents.filter { agent ->
            agent.state.chatId == currentChatId
        }

        if (agentsInChat.isEmpty()) {
            ui.out("No agents wired to the current chat")
            return
        }

        val output = buildString {
            appendLine("Agents Stats for Current Chat:")
            appendLine("=" .repeat(60))
            appendLine()

            agentsInChat.forEach { agent ->
                val messagesCount = agent.state.context.messages.size
                val contextSize = agent.state.context.messages.sumOf { it.content.length }

                val agentName = agent.state.name.ifEmpty {
                    "Unnamed Agent (${agent.id.value.take(8)})"
                }

                appendLine("Agent: $agentName")
                appendLine("-".repeat(60))
                appendLine("  Messages Count: $messagesCount")
                appendLine("  Context Size:   $contextSize symbols")
                appendLine()
            }

            appendLine("=" .repeat(60))
            appendLine("Total Agents: ${agentsInChat.size}")
        }

        ui.out(output)
    }
}
