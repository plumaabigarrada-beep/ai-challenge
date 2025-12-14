package com.jamycake.aiagent.app.commands.chat

import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.terminal.Command

internal class SendMessageCommand(
    private val getCurrentUser: () -> User?
) : Command(values = listOf("--send")) {

    override suspend fun execute(args: String?) {
        if (args.isNullOrEmpty()) return

        val user = getCurrentUser()
        user?.sendMessage(args)
    }
}