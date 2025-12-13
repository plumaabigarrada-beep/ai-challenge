package com.jamycake.aiagent.app

import com.jamycake.aiagent.app.commands.agents.agentsCommand
import com.jamycake.aiagent.app.commands.chat.chatCommands
import com.jamycake.aiagent.app.commands.stats.statsCommands
import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.TerminalUI

internal fun commands(
    user: User,
    stats: Stats,
    terminalUI: TerminalUI,
    agents: Agents,
    agent: Agent
) : List<Command> {
    val chatCommands = chatCommands(user)
    val statsCommands = statsCommands(stats = stats, terminalUI)
    val agentsCommands = agentsCommand(agents = agents, agent = agent)

    val allCommands = chatCommands + statsCommands + agentsCommands

    return allCommands
}