package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.terminal.Command

internal class SaveAgentsCommand(
    private val agents: Agents,
    private val currentAgents: () -> List<Agent>
) : Command(values = listOf("--save-agent")){

    override suspend fun execute(args: String?) {
        currentAgents.invoke().forEach {
            agents.save(it)
        }
    }
}