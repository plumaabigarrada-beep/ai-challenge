package com.jamycake.aiagent.app.commands.chat


import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.terminal.Command

internal class SendCommand(
    private val user: User
) : Command(values = listOf("--send")) {

    override suspend fun execute(args: String?) {

        if (args.isNullOrEmpty()) return
        user.sendMessage(args)
    }
}