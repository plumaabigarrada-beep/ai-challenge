package chatcontainer

import chat.Chat
import client.Client
import org.example.ClientType
import org.example.Config

/**
 * ChatContainer data container.
 * Business logic is handled by command classes.
 */
internal class ChatContainer(
    internal val chats: MutableMap<String, Chat>,
    internal val clients: Map<ClientType, Client>,
    internal var currentChatId: String,
    internal val defaultConfig: Config,
) {

    val size get() = chats.size

    fun getCurrentChat(): Chat? {
        return chats[currentChatId]
    }

    fun getChats(): Map<String, Chat> {
        return chats.toMap()
    }

    fun getCurrentChatId(): String {
        return currentChatId
    }

    fun findChatByPartialId(partialId: String): String? {
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