package factories

import chat.Chat
import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import commands.SendMessageCommand
import compressor.COMPRESS_PTOMPT
import compressor.ContextCompressor
import org.example.App
import org.example.ClientType
import org.example.Config
import org.example.GeneralClient
import org.example.context.Context
import org.example.contextsender.ContextSender

internal fun createApp() : App {

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
        clients = clients,
        config = config,
        context = Context(messages = emptyList()),
    )

    val chatContainer = ChatContainer(
        chats = mutableMapOf(defaultChat.id to defaultChat),
        clients = clients,
        currentChatId = defaultChat.id,
        defaultConfig = config,
    )


    val sendMessageCommand = SendMessageCommand(
        chatContainer = chatContainer,
        contextCompressor = compressor,
        contextSender = contextSender,
        values = listOf("--send")
    )

    val chatCommands = chatCommands(sendMessageCommand, chatContainer, compressor)
    val configurationCommands = getConfigurationCommands(config, clients)
    val fileCommands = getFileCommands(chatContainer, chatSaver, sendMessageCommand)

    val commands = chatCommands + configurationCommands + fileCommands

    return App(
        clients = clients,
        commands = commands,
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
    )

}


