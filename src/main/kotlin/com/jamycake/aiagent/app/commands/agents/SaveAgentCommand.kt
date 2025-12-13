package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.terminal.Command

internal class SaveAgentCommand(
    private val agents: Agents,
    private val agent: Agent
) : Command(values = listOf("--save-agent")){

    override suspend fun execute(args: String?) {
        agents.save(agent)
    }
}