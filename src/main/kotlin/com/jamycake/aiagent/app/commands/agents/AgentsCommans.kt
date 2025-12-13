package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.terminal.Command

internal fun agentsCommand(
    agents: Agents,
    agent: Agent
) : List<Command> {


    return listOf(
        SaveAgentCommand(
            agents = agents,
            agent = agent
        )
    )

}