package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.TerminalUI

internal fun spaceCommands(
    agents: Agents,
    space: Space,
    chats: Chats,
    users: Users,
    terminalUI: TerminalUI
) : List<Command> {
    return listOf(
        RestoreAgentCommand(
            agents,
            chats,
            users,
            space,
            terminalUI,
        )
    )
}