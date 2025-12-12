package factories

import chatcontainer.ChatContainer
import chatsaver.ChatSaver
import commands.ReadAndSendFileCommand
import commands.SaveChatCommand
import org.example.Command

internal fun getFileCommands(
    chatContainer: ChatContainer,
    chatSaver: ChatSaver
): List<Command> = listOf(
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
