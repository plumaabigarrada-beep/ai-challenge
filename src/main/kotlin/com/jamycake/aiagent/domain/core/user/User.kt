package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage

internal class User(
    val chatMemberId: ChatMemberId = ChatMemberId(),
    val chats: Map<String, Chat>
) {

    private var _chatId: String = chats.keys.first()
    val chatId: String get() = _chatId

    suspend fun sendMessage(message: String) {
        val message = ChatMessage(
            role = "user",
            content = message,
        )

        val chat = chats[_chatId]
        chat?.sendMessage(chatMemberId, message)
    }

    fun switchChat(chatId: String) {
        _chatId = chatId
    }

}