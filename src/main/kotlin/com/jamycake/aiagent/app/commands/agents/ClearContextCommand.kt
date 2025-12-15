package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class ClearContextCommand(
    private val space: Space,
    private val agents: Agents,
    private val ui: UI
) : Command(values = listOf("--clear-context")) {

    override suspend fun execute(args: String?) {
        val agent = space.currentAgent
        if (agent == null) {
            ui.out("No agent selected. Use --select-agent first")
            return
        }

        agent.state.context.clearMessages()
        agents.save(agent)
        ui.out("Context cleared for agent: ${agent.state.name}")
    }
}
