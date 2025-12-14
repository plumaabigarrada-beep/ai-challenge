package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.terminal.Command

internal class CreateNewAgentCommand(
    private val agents: Agents,
    private val addAgent: (Agent) -> Unit,
    private val outMessage: (String) -> Unit
) : Command(values = listOf("--new-agent")){


    override suspend fun execute(args: String?) {

        if (args.isNullOrEmpty()) {
            outMessage("The name must be provided")
            return
        }

        val newAgent = agents.new()

        newAgent.state.name = args

        addAgent(newAgent)

        outMessage("Agent ${newAgent.state.name} added")


    }
}