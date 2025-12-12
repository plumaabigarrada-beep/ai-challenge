package factories

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.chatsaver.ChatSaver
import com.jamycake.aiagent.terminal.commands.ReadAndSendFileCommand
import com.jamycake.aiagent.terminal.commands.SaveChatCommand
import com.jamycake.aiagent.terminal.commands.SendMessageCommand
import com.jamycake.aiagent.terminal.Command

internal fun getFileCommands(
    chatContainer: ChatContainer,
    chatSaver: ChatSaver,
    sendMessageCommand: com.jamycake.aiagent.terminal.commands.SendMessageCommand
): List<Command> = listOf(
    // File and message commands
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ReadAndSendFileCommand(
        chatContainer = chatContainer,
        sendMessageCommand = sendMessageCommand,
        values = listOf("--file", "-f")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SaveChatCommand(
        chatContainer = chatContainer,
        chatSaver = chatSaver,
        values = listOf("--save", "-s")
    )
)
