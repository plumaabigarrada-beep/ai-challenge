package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage

internal class User(
    val id: UserId = UserId.empty(),
    val chatId: ChatId = ChatId.empty(),
    val chatMemberId: ChatMemberId = ChatMemberId(),
    val chat: (ChatId) -> Chat?
) {

    suspend fun sendMessage(message: String) {
        val message = ChatMessage(
            role = "user",
            content = message,
        )

        val chat = chat(ChatId.empty())
        chat?.sendMessage(chatMemberId, message)
    }

}