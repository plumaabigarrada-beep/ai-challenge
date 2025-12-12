package com.jamycake.aiagent.factories

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.chatsaver.ChatSaver
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.commands.ReadAndSendFileCommand
import com.jamycake.aiagent.terminal.commands.SaveChatCommand
import com.jamycake.aiagent.terminal.commands.SendMessageCommand

internal fun getFileCommands(
    chatContainer: ChatContainer,
    chatSaver: ChatSaver,
    sendMessageCommand: SendMessageCommand
): List<Command> = listOf(
    // File and message commands
    ReadAndSendFileCommand(
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
        values = listOf("--file", "-f")
    ),
    SaveChatCommand(
        chatContainer = chatContainer,
        chatSaver = chatSaver,
        values = listOf("--save", "-s")
    )
)
