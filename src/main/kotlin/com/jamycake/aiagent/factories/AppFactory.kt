package com.jamycake.aiagent.factories

import com.jamycake.aiagent.chat.Chat
import com.jamycake.aiagent.chat.ClientType
import com.jamycake.aiagent.chat.Config
import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.chatsaver.ChatSaver
import com.jamycake.aiagent.client.impl.GeneralClient
import com.jamycake.aiagent.compressor.COMPRESS_PTOMPT
import com.jamycake.aiagent.compressor.ContextCompressor
import com.jamycake.aiagent.context.Context
import com.jamycake.aiagent.contextsender.ContextSender
import com.jamycake.aiagent.terminal.app.TerminalApp

internal fun createApp() : TerminalApp {

    val perplexityClient = GeneralClient(baseUrl = "https://api.perplexity.ai/chat/completions")
    val huggingFaceClient = GeneralClient(baseUrl = "https://router.huggingface.co/v1/chat/completions")
    val lmstudioClient = GeneralClient(baseUrl = "http://localhost:1234/v1/chat/completions")

    val clients = mapOf(
        ClientType.LMSTUDIO to lmstudioClient,
        ClientType.PERPLEXITY to perplexityClient,
        ClientType.HUGGINGFACE to huggingFaceClient,
    )

    val chatSaver = ChatSaver()

    val config = Config(
        model = lmstudioClient.models().first()
    )

    // Create ContextSender with all clients
    val contextSender = ContextSender(clients = clients)

    val compressor = ContextCompressor(
        contextSender = contextSender,
        compressPrompt = COMPRESS_PTOMPT,
        defaultClientType = ClientType.LMSTUDIO,
        defaultModel = config.model,
    )

    val defaultChat = Chat(
        config = config,
        context = Context(messages = emptyList()),
    )

    val chatContainer = ChatContainer(
        chats = mutableMapOf(defaultChat.id to defaultChat),
        clients = clients,
        currentChatId = defaultChat.id,
        defaultConfig = config,
    )

    val compressContextCommand = _root_ide_package_.com.jamycake.aiagent.terminal.commands.CompressContextCommand(
        chatContainer = chatContainer,
        contextCompressor = compressor,
        values = listOf("--compress", "-cp")
    )


    val sendMessageCommand = _root_ide_package_.com.jamycake.aiagent.terminal.commands.SendMessageCommand(
        chatContainer = chatContainer,
        contextSender = contextSender,
        values = listOf("--send"),
        compressContextCommand = compressContextCommand,
    )

    val chatCommands = chatCommands(sendMessageCommand, chatContainer, compressContextCommand)
    val configurationCommands = getConfigurationCommands(config, clients)
    val fileCommands = getFileCommands(chatContainer, chatSaver, sendMessageCommand)

    val commands = chatCommands + configurationCommands + fileCommands

    return TerminalApp(
        clients = clients,
        commands = commands,
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
    )

}


