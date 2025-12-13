package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.core.user.UserId
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    user: (UserId) -> User?,
    focusManager: FocusManager
) : List<Command>{

    return listOf(
        SendMessageCommand(
            user = user,
            focusManager = focusManager
        )
    )
}