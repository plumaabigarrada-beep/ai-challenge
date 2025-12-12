package com.jamycake.aiagent.factories

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.commands.*

internal fun chatCommands(
    sendMessageCommand: SendMessageCommand,
    chatContainer: ChatContainer,
    compressContextCommand: CompressContextCommand
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
    compressContextCommand,
    GetConfigCommand(
        chatContainer = chatContainer,
        values = listOf("--config", "-c")
    ),
    GetContextCommand(
        chatContainer = chatContainer,
        values = listOf("--context", "-ctx")
    ),
)