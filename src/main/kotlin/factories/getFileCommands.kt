package factories

import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import commands.ReadAndSendFileCommand
import commands.SaveChatCommand
import commands.SendMessageCommand
import org.example.Command

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
