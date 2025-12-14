package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage

internal class User(
    val id: UserId = UserId.empty(),
    val focusManager: FocusManager,
    val chatMemberId: ChatMemberId = ChatMemberId(),
    val chat: (ChatId) -> Chat?
) {

    val chatId: ChatId get() = focusManager.chatId


    suspend fun sendMessage(message: String) {
        val message = ChatMessage(
            role = "user",
            name = "User",
            content = message,
        )

        val chat = chat(focusManager.chatId)
        chat?.sendMessage(chatMemberId, message)
    }

}