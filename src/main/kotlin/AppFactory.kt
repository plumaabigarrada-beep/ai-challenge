import chat.Chat
import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import commands.*
import compressor.COMPRESS_PTOMPT
import compressor.ChatCompressor
import org.example.App
import org.example.ClientType
import org.example.Command
import org.example.Config
import org.example.HuggingFaceClient
import org.example.LMStudioClient
import org.example.PerplexityClient

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
    val commands = listOf(
        sendMessageCommand,
        // Help command
        HelpCommand(
            values = listOf("--help", "-h")
        ),

        // Chat management commands
        CreateChatCommand(
            chatContainer = chatContainer,
            values = listOf("--newchat", "-nc")
        ),
        DeleteChatCommand(
            chatContainer = chatContainer,
            values = listOf("--deletechat", "-dc")
        ),
        SwitchChatCommand(
            chatContainer = chatContainer,
            values = listOf("--switchchat", "-sc")
        ),
        ListChatsCommand(
            chatContainer = chatContainer,
            values = listOf("--chats", "-lc")
        ),
        RenameChatCommand(
            chatContainer = chatContainer,
            values = listOf("--renamechat", "-rc")
        ),
        ClearHistoryCommand(
            chatContainer = chatContainer,
            values = listOf("clear", "reset")
        ),
        CompressChatCommand(
            chatContainer = chatContainer,
            chatCompressor = compressor,
            values = listOf("--compress", "-cp")
        ),
        GetConfigCommand(
            chatContainer = chatContainer,
            values = listOf("--config", "-c")
        ),

        // Configuration commands
        SetClientCommand(
            config = config,
            clients = clients,
            values = listOf("--client", "-cl")
        ),
        SetModelCommand(
            config = config,
            values = listOf("--model", "-m")
        ),
        SetTemperatureCommand(
            config = config,
            values = listOf("--temperature", "-t")
        ),
        SetSystemPromptCommand(
            config = config,
            values = listOf("--systemprompt", "-sp")
        ),
        ToggleShowTokensCommand(
            config = config,
            values = listOf("--showtokens", "-st")
        ),
        ToggleAutoCompressCommand(
            config = config,
            values = listOf("--autocompress", "-ac")
        ),
        SetAutoCompressThresholdCommand(
            config = config,
            values = listOf("--acthreshold", "-act")
        ),
        ListModelsCommand(
            config = config,
            clients = clients,
            values = listOf("--models", "-ls")
        ),

        // File and message commands
        ReadAndSendFileCommand(
            chatContainer = chatContainer,
            values = listOf("--file", "-f")
        ),
        SaveChatCommand(
            chatContainer = chatContainer,
            chatSaver = chatSaver,
            values = listOf("--save", "-s")
        )
    )

    return App(
        clients = clients,
        commands = commands,
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
    )

}