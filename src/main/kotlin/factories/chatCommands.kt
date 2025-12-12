package factories

import chatcontainer.ChatContainer
import commands.*
import compressor.ContextCompressor
import org.example.Command

internal fun chatCommands(
    sendMessageCommand: SendMessageCommand,
    chatContainer: ChatContainer,
    compressor: ContextCompressor
): List<Command> = listOf(
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
        contextCompressor = compressor,
        values = listOf("--compress", "-cp")
    ),
    GetConfigCommand(
        chatContainer = chatContainer,
        values = listOf("--config", "-c")
    ),
)