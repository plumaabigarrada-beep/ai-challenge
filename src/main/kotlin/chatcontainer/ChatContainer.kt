package chatcontainer

import chat.Chat
import chatsaver.ChatSaver
import client.Client
import compressor.ChatCompressor
import org.example.ClientType
import org.example.Config

class ChatContainer(
    private val chats: MutableMap<String, Chat>,
    private val clients: Map<ClientType, Client>,
    private var currentChatId: String,
    private val chatCompressor: ChatCompressor,
    private val defaultConfig: Config,
    private val chatSaver: ChatSaver
) {

    val size get() = chats.size

    fun newChat(name: String?): Chat {
        val newChat = Chat(
            name = name ?: "Chat ${chats.size + 1}",
            clients = clients,
            config = defaultConfig.copy(),
            chatCompressor = chatCompressor,
            saver = chatSaver
        )

        currentChatId = newChat.id

        chats[newChat.id] = newChat

        return newChat

    }

    fun switchChat(chatId: String): String {
        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        currentChatId = fullChatId
        val currentChat = chats[fullChatId]
        val message = "Switched to chat: ${currentChat?.name}\n"

        return message
    }

    fun deleteChat(chatId: String): String {
        if (chats.size == 1) {
            return "Cannot delete the last chat. Create a new one first.\n"
        }

        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        var newCurrentChatId = currentChatId
        if (fullChatId == currentChatId) {
            val anotherChatId = chats.keys.first { it != fullChatId }
            newCurrentChatId = anotherChatId
            currentChatId = newCurrentChatId
        }

        val deletedChat = chats.remove(fullChatId)
        val currentChat = chats[newCurrentChatId]
        val message = "Deleted chat: ${deletedChat?.name}. Switched to: ${currentChat?.name}\n"

        return message
    }

    fun getCurrentChat(): Chat? {
        return chats[currentChatId]
    }

    fun getChats(): Map<String, Chat> {
        return chats.toMap()
    }

    fun getCurrentChatId(): String {
        return currentChatId
    }

    private fun findChatByPartialId(partialId: String): String? {
        // First try exact match
        if (chats.containsKey(partialId)) {
            return partialId
        }

        // Then try partial match (case-insensitive)
        val matches = chats.keys.filter { it.startsWith(partialId, ignoreCase = true) }

        return when {
            matches.isEmpty() -> null
            matches.size == 1 -> matches.first()
            else -> {
                // Multiple matches - return exact prefix match if available
                matches.firstOrNull { it.startsWith(partialId, ignoreCase = false) }
                    ?: matches.first() // Otherwise return first match
            }
        }
    }

}