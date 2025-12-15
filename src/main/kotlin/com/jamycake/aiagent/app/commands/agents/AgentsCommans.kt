package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.tools.Tools
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal fun agentsCommand(
    agents: Agents,
    addAgent: (Agent) -> Unit,
    currentAgents: () -> List<Agent>,
    outMessage: (String) -> Unit,
    space: Space,
    tools: Tools,
    ui: UI
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
        ),
        SelectAgentCommand(
            space = space,
            ui = ui
        ),
        ClearContextCommand(
            space = space,
            agents = agents,
            ui = ui
        ),
        CurrentAgentCommand(
            space = space,
            agents = agents,
            tools = tools,
            ui = ui
        ),
        ToolsCommand(
            tools = tools,
            ui = ui
        )
    )

}