package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage

internal class User(
    val id: UserId = UserId.empty(),
    var chatId: ChatId = ChatId.empty(),
    val chatMemberId: ChatMemberId = ChatMemberId(),
    val chat: (ChatId) -> Chat?
) {

    suspend fun sendMessage(message: String) {
        val message = ChatMessage(
            role = "user",
            name = "User",
            content = message,
        )

        val chat = chat(chatId)
        chat?.sendMessage(chatMemberId, message)
    }

}