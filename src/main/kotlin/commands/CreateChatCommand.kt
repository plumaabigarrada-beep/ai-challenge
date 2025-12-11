package commands

import chat.Chat
import client.Client
import compressor.ChatCompressor
import org.example.ClientType
import org.example.Config

class CreateChatCommand {
    fun execute(
        chats: MutableMap<String, Chat>,
        clients: Map<ClientType, Client>,
        config: Config,
        chatCompressor: ChatCompressor,
        name: String?
    ): Pair<String, String> {
        val newChat = Chat(
            name = name ?: "Chat ${chats.size + 1}",
            clients = clients,
            config = config,
            chatCompressor = chatCompressor
        )
        chats[newChat.id] = newChat
        val newChatId = newChat.id
        val message = "Created and switched to new chat: ${newChat.name}\n"
        return Pair(newChatId, message)
    }
}
