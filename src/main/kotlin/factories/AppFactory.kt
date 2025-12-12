package factories

import chat.Chat
import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import commands.SendMessageCommand
import compressor.COMPRESS_PTOMPT
import compressor.ChatCompressor
import org.example.*

fun createApp() : App {

    val perplexityClient = PerplexityClient()
    val huggingFaceClient = HuggingFaceClient()
    val lmStudioClient = LMStudioClient()

    val clients = mapOf(
        ClientType.LMSTUDIO to lmStudioClient,
        ClientType.PERPLEXITY to perplexityClient,
        ClientType.HUGGINGFACE to huggingFaceClient,
    )

    val compressor = ChatCompressor(
        client = lmStudioClient,
        compressPrompt = COMPRESS_PTOMPT,
    )

    val chatSaver = ChatSaver()

    val config = Config(
        model = lmStudioClient.models().first()
    )

    val defaultChat = Chat(
        clients = clients,
        config = config,
        chatCompressor = compressor,
        saver = chatSaver
    )

    val chatContainer = ChatContainer(
        chats = mutableMapOf(defaultChat.id to defaultChat),
        clients = clients,
        currentChatId = defaultChat.id,
        chatCompressor = compressor,
        defaultConfig = config,
        chatSaver = chatSaver,
    )


    val sendMessageCommand = SendMessageCommand(
        chatContainer = chatContainer,
        values = listOf("--send")
    )

    val chatCommands = chatCommands(sendMessageCommand, chatContainer, compressor)
    val configurationCommands = getConfigurationCommands(config, clients)
    val fileCommands = getFileCommands(chatContainer, chatSaver)

    val commands = chatCommands + configurationCommands + fileCommands

    return App(
        clients = clients,
        commands = commands,
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
    )

}


