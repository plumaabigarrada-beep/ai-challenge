package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    getCurrentUser: () -> User?,
    allChats: () -> List<Chat>,
    space: Space,
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
            ui = ui
        ),
        CurrentChatCommand(
            allChats = allChats,
            getCurrentUser = getCurrentUser,
            ui = ui
        )
    )
}