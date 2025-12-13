package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    user: User
) : List<Command>{

    return listOf(
        SendCommand(user = user)
    )
}