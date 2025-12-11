package commands

import chat.Chat
import chatcontainer.ChatContainer
import client.Client
import compressor.ChatCompressor
import org.example.ClientType
import org.example.Command
import org.example.Config
import java.time.chrono.JapaneseEra.values

class CreateChatCommand(
    private val chatContainer: ChatContainer,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val newChat = chatContainer.newChat(args)
        return "Created and switched to new chat: ${newChat.name}\n"
    }
}
