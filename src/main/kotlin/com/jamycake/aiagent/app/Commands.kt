package com.jamycake.aiagent.app

import com.jamycake.aiagent.app.commands.agents.agentsCommand
import com.jamycake.aiagent.app.commands.chat.chatCommands
import com.jamycake.aiagent.app.commands.space.spaceCommands
import com.jamycake.aiagent.app.commands.stats.statsCommands
import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.TerminalUI

internal fun commands(
    chats: Chats,
    users: Users,
    stats: Stats,
    terminalUI: UI,
    agents: Agents,
    space: Space,
    focusManager: FocusManager
) : List<Command> {
    val chatCommands = chatCommands(
        user = space::getUser,
        focusManager = focusManager
    )
    val statsCommands = statsCommands(stats = stats, terminalUI)
    val agentsCommands = agentsCommand(agents = agents, currentAgents = space::allAgents)
    val spaceCommands = spaceCommands(
        agents,
        space,
        chats,
        users,
        terminalUI,
    )

    val allCommands = chatCommands + statsCommands + agentsCommands + spaceCommands

    return allCommands
}