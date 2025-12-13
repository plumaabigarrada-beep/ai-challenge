package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.space.Space
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
        val chat = chats.getAllChats()
        val user = users.get()


        if (agents.isEmpty()) {
            ui.out("No agents")
            return
        }
        agents.forEach {
            space.addAgent(it)
        }

        space.addChats(chat)
        space.addUser(user)

        space.wire()
    }

    companion object {
        const val name = "--restore-agent"
    }
}