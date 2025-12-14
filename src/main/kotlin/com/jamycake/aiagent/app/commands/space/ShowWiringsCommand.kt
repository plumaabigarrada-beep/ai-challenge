package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class ShowWiringsCommand(
    private val allAgents: () -> List<Agent>,
    private val allChats: () -> List<Chat>,
    private val ui: UI
) : Command(listOf("--wirings")) {

    override suspend fun execute(args: String?) {
        val agents = allAgents()
        val chats = allChats()

        if (chats.isEmpty()) {
            ui.out("No chats available")
            return
        }

        val output = buildString {
            appendLine("Chat-Agent Wirings:")
            appendLine("===================")
            appendLine()

            chats.forEach { chat ->
                appendLine("Chat ID: ${chat.id.value}")
                val wiredAgents = agents.filter { it.state.chatId == chat.id }

                if (wiredAgents.isEmpty()) {
                    appendLine("  No agents wired to this chat")
                } else {
                    wiredAgents.forEach { agent ->
                        appendLine("  -> Agent: ${agent.state.name} (ID: ${agent.id.value})")
                    }
                }
                appendLine()
            }

            appendLine("===================")
            appendLine("Total chats: ${chats.size}")
            appendLine("Total agents: ${agents.size}")
        }

        ui.out(output)
    }
}
