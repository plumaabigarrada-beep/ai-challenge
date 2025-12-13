package com.jamycake.aiagent.app

import com.jamycake.aiagent.data.AgentsImpl
import com.jamycake.aiagent.data.GeneralClient
import com.jamycake.aiagent.data.RamStats
import com.jamycake.aiagent.domain.core.agent.*
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.terminal.Terminal
import com.jamycake.aiagent.terminal.TerminalUI

internal fun createApp() : App {

    val perplexityClient = GeneralClient(baseUrl = "https://api.perplexity.ai/chat/completions")
    val huggingFaceClient = GeneralClient(baseUrl = "https://router.huggingface.co/v1/chat/completions")
    val lmstudioClient = GeneralClient(baseUrl = "http://localhost:1234/v1/chat/completions")

    val clients = mapOf<ClientType, Client>(
        ClientType.LMSTUDIO to lmstudioClient,
        ClientType.PERPLEXITY to perplexityClient,
        ClientType.HUGGINGFACE to huggingFaceClient
    )

    val stats = RamStats()

    val chat = Chat()

    val chats = mapOf(chat.id to chat)


    val agent = defauldAgent(clients, chats, stats)

    val agents: Agents = AgentsImpl(
        clients = clients,
        chats = chats,
        stats = stats,
    )

    val user = User(
        chats = chats
    )

    val terminalUI = TerminalUI()

    chat.addMember(agent.chatMemberId) { chatMessage -> agent.updateContext(chatMessage) }

    chat.addMember(user.chatMemberId) { terminalUI.sendMessage(it.content) }


    val allCommands = commands(
        user = user,
        stats = stats,
        terminalUI = terminalUI,
        agents = agents,
        agent = agent,
    )

    val terminal = Terminal(
        onNoCommands = { user.sendMessage(it) },
        commands = allCommands
    )

    val app = App(
        clients = clients,
        terminal = terminal
    )

    return app


}

private fun defauldAgent(
    clients: Map<ClientType, Client>,
    chats: Map<String, Chat>,
    stats: RamStats
): Agent {
    val agentState = AgentState(
        name = "",
        config = Config(temperature = 0.7),
        context = Context(messages = emptyList()),
    )

    val agent = Agent(
        state = agentState,
        clients = clients,
        chats = chats,
        stats = stats,
    )
    return agent
}


