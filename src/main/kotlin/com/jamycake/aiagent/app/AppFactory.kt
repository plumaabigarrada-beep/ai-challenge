package com.jamycake.aiagent.app

import com.jamycake.aiagent.data.*
import com.jamycake.aiagent.domain.core.agent.ClientType
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Chats
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Users
import com.jamycake.aiagent.domain.space.Space
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

    val terminalUI = TerminalUI()
    val space = Space(ui = terminalUI)

    val agents: Agents = AgentsImpl(
        clients = clients,
        space = space,
        stats = stats,
    )

    val users: Users = UsersImpl()
    val chats: Chats = ChatsImpl()

    val allCommands = commands(
        stats = stats,
        terminalUI = terminalUI,
        agents = agents,
        space = space,
        chats = chats,
        users = users
    )

    val terminal = Terminal(
        onNoCommands = { space.currentUser?.sendMessage(it) },
        commands = allCommands
    )

    val app = App(
        clients = clients,
        terminal = terminal
    )

    return app
}
