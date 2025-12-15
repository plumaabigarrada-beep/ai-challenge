package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command


internal class RestoreAppStateCommand(
    private val agents: Agents,
    private val chats: Chats,
    private val users: Users,
    private val space: Space,
    private val ui: UI
) : Command(values = listOf(name)) {


    override suspend fun execute(args: String?) {
        val agents = this@RestoreAppStateCommand.agents.get()
        val chats = this@RestoreAppStateCommand.chats.getAllChats()
        val userState = users.get()


        if (agents.isEmpty()) {
            ui.out("No agents")
        }
        agents.forEach {
            space.addAgent(it)
        }


        val user = User.from(
            userState = userState,
            chat = space::getChat
        )


        space::getChat

        space.addChats(chats)
        space.addUser(user)

        space.wire()
    }

    companion object {
        const val name = "--restore-agent"
    }
}