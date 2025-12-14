package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class ListAgentsCommand(
    private val allAgents: () -> List<Agent>,
    private val ui: UI
) : Command(listOf("--agents")) {

    override suspend fun execute(args: String?) {
        val agents = allAgents()

        if (agents.isEmpty()) {
            ui.out("No agents available")
            return
        }

        val output = buildString {
            appendLine("Agents:")
            appendLine("------")
            agents.forEach { agent ->
                appendLine("Name: ${agent.state.name}")
                appendLine("  ID: ${agent.id.value}")
                appendLine("  Chat ID: ${agent.state.chatId?.value}")
                appendLine("  Model: ${agent.state.config.model}")
                appendLine("  Client: ${agent.state.config.clientType}")
                appendLine()
            }
            appendLine("------")
            appendLine("Total: ${agents.size}")
        }

        ui.out(output)
    }
}