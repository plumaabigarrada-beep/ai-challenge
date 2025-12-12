package factories

import com.jamycake.aiagent.chatcontainer.ChatContainer
import commands.*
import com.jamycake.aiagent.terminal.Command

internal fun chatCommands(
    sendMessageCommand: com.jamycake.aiagent.terminal.commands.SendMessageCommand,
    chatContainer: ChatContainer,
    compressContextCommand: com.jamycake.aiagent.terminal.commands.CompressContextCommand
): List<Command> = listOf(
    sendMessageCommand,
    // Help command
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.HelpCommand(
        values = listOf("--help", "-h")
    ),

    // Chat management commands
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.CreateChatCommand(
        chatContainer = chatContainer,
        values = listOf("--newchat", "-nc")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.DeleteChatCommand(
        chatContainer = chatContainer,
        values = listOf("--deletechat", "-dc")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SwitchChatCommand(
        chatContainer = chatContainer,
        values = listOf("--switchchat", "-sc")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ListChatsCommand(
        chatContainer = chatContainer,
        values = listOf("--chats", "-lc")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.RenameChatCommand(
        chatContainer = chatContainer,
        values = listOf("--renamechat", "-rc")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ClearHistoryCommand(
        chatContainer = chatContainer,
        values = listOf("clear", "reset")
    ),
    compressContextCommand,
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.GetConfigCommand(
        chatContainer = chatContainer,
        values = listOf("--config", "-c")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.GetContextCommand(
        chatContainer = chatContainer,
        values = listOf("--context", "-ctx")
    ),
)