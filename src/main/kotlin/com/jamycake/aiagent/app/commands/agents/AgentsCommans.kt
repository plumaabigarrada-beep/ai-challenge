package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.terminal.Command

internal fun agentsCommand(
    agents: Agents,
    addAgent: (Agent) -> Unit,
    currentAgents: () -> List<Agent>,
    outMessage: (String) -> Unit
) : List<Command> {


    return listOf(
        SaveAgentsCommand(
            agents = agents,
            currentAgents = currentAgents
        ),
        CreateNewAgentCommand(
            agents = agents,
            addAgent = addAgent,
            outMessage = outMessage
        )
    )

}