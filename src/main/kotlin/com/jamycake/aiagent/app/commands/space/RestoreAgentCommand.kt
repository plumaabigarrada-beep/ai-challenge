package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.space.Space
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.TerminalUI


internal class RestoreAgentCommand(
    private val agents: Agents,
    private val chats: Chats,
    private val users: Users,
    private val space: Space,
    private val terminalUI: TerminalUI
) : Command(values = listOf(name)) {


    override suspend fun execute(args: String?) {
        val agent = agents.get()
        val chat = chats.getChat()
        val user = users.get()


        if (agent == null) {
            terminalUI.out("No agent")
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