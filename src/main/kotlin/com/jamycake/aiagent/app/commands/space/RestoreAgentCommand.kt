package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command


internal class RestoreAgentCommand(
    private val agents: Agents,
    private val chats: Chats,
    private val users: Users,
    private val space: Space,
    private val ui: UI
) : Command(values = listOf(name)) {


    override suspend fun execute(args: String?) {
        val agent = agents.get()
        val chat = chats.getChat()
        val user = users.get()


        if (agent == null) {
            ui.out("No agent")
            return
        }
        agent.forEach {
            space.addAgent(it)
        }

        space.addChat(chat)
        space.addUser(user)

        space.wire()
    }

    companion object {
        const val name = "--restore-agent"
    }
}