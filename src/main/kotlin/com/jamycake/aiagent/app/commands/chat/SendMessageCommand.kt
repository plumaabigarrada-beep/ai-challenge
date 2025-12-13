package com.jamycake.aiagent.app.commands.chat


import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.core.user.UserId
import com.jamycake.aiagent.terminal.Command

internal class SendMessageCommand(
    private val focusManager: FocusManager,
    private val user: (UserId) -> User?
) : Command(values = listOf("--send")) {

    override suspend fun execute(args: String?) {

        if (args.isNullOrEmpty()) return

        val user = user(focusManager.userid)
        user?.sendMessage(args)
    }
}