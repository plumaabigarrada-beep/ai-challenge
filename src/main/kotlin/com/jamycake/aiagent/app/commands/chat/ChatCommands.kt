package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.core.user.UserId
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    user: (UserId) -> User?,
    focusManager: FocusManager,
    allChats: () -> List<Chat>,
    space: Space,
    ui: UI
) : List<Command>{

    return listOf(
        SendMessageCommand(
            user = user,
            focusManager = focusManager
        ),
        SwitchChatCommand(
            allChats = allChats,
            focusManager = focusManager,
            space = space,
            ui = ui
        ),
        CurrentChatCommand(
            allChats = allChats,
            focusManager = focusManager,
            ui = ui
        )
    )
}