package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    getCurrentUser: () -> User?,
    allChats: () -> List<Chat>,
    space: Space,
    chats: Chats,
    agents: Agents,
    users: Users,
    ui: UI
) : List<Command>{

    return listOf(
        SendMessageCommand(
            getCurrentUser = getCurrentUser
        ),
        SwitchChatCommand(
            allChats = allChats,
            getCurrentUser = getCurrentUser,
            space = space,
            users = users,
            ui = ui
        ),
        CurrentChatCommand(
            allChats = allChats,
            getCurrentUser = getCurrentUser,
            ui = ui
        ),
        ChatNameCommand(
            allChats = allChats,
            getCurrentUser = getCurrentUser,
            ui = ui
        ),
        DeleteChatCommand(
            allChats = allChats,
            getCurrentUser = getCurrentUser,
            space = space,
            chats = chats,
            agents = agents,
            ui = ui
        ),
        AgentsStatsCommand(
            getCurrentUser = getCurrentUser,
            space = space,
            ui = ui
        )
    )
}