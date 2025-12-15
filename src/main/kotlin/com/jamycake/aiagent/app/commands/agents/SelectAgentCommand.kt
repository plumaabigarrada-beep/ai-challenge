package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class SelectAgentCommand(
    private val space: Space,
    private val ui: UI
) : Command(values = listOf("--select-agent")) {

    override suspend fun execute(args: String?) {
        if (args.isNullOrEmpty()) {
            ui.out("Agent name or ID must be provided")
            return
        }

        val agent = space.allAgents.find {
            it.state.name == args || it.id.value == args
        }

        if (agent == null) {
            ui.out("Agent not found: $args")
            return
        }

        space.currentAgent = agent
        ui.out("Selected agent: ${agent.state.name} (${agent.id.value})")
    }
}
