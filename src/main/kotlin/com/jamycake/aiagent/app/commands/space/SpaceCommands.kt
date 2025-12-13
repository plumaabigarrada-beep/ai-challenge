package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.app.commands.space.ListAgentsCommand
import com.jamycake.aiagent.app.commands.space.ListChatsCommand
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal fun spaceCommands(
    agents: Agents,
    space: Space,
    chats: Chats,
    users: Users,
    terminalUI: UI
) : List<Command> {
    return listOf(
        RestoreAgentCommand(
            agents,
            chats,
            users,
            space,
            terminalUI,
        ),
        ListChatsCommand(
            allChats = space::allChats,
            ui = terminalUI
        ),
        ListAgentsCommand(
            allAgents = space::allAgents,
            ui = terminalUI
        ),
        ShowWiringsCommand(
            allAgents = space::allAgents,
            allChats = space::allChats,
            ui = terminalUI
        ),
        WireAgentToChatCommand(
            allAgents = space::allAgents,
            allChats = space::allChats,
            space = space,
            ui = terminalUI
        )
    )
}