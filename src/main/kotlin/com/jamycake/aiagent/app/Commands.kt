package com.jamycake.aiagent.app

import com.jamycake.aiagent.app.commands.agents.agentsCommand
import com.jamycake.aiagent.app.commands.chat.chatCommands
import com.jamycake.aiagent.app.commands.help.HelpCommand
import com.jamycake.aiagent.app.commands.space.spaceCommands
import com.jamycake.aiagent.app.commands.stats.statsCommands
import com.jamycake.aiagent.domain.slots.*
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal fun commands(
    chats: Chats,
    users: Users,
    stats: Stats,
    terminalUI: UI,
    agents: Agents,
    space: Space
) : List<Command> {
    val chatCommands = chatCommands(
        getCurrentUser = { space.currentUser },
        allChats = space::allChats,
        space = space,
        ui = terminalUI
    )
    val statsCommands = statsCommands(stats = stats, terminalUI)
    val agentsCommands = agentsCommand(
        agents = agents,
        currentAgents = space::allAgents,
        addAgent = space::addAgent,
        outMessage = terminalUI::out
    )
    val spaceCommands = spaceCommands(
        agents,
        space,
        chats,
        users,
        terminalUI,
    )

    // Create list without help command first
    val commandsWithoutHelp = chatCommands + statsCommands + agentsCommands + spaceCommands

    // Create help command with reference to all commands
    val helpCommand = HelpCommand(
        allCommands = { commandsWithoutHelp },
        ui = terminalUI
    )

    val allCommands = commandsWithoutHelp + helpCommand

    return allCommands
}