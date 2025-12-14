package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage

internal class User private constructor(
    val id: UserId = UserId.empty(),
    var chatId: ChatId?,
    var chatMemberId: ChatMemberId?,
    private val chat: (ChatId) -> Chat?
) {



    suspend fun sendMessage(message: String) {
        val message = ChatMessage(
            role = "user",
            name = "User",
            content = message,
        )

        if (chatId == null) return
        if (chatMemberId == null) return
        val chat = chat(chatId!!)
        chat?.sendMessage(chatMemberId!!, message)
    }

    fun getState() : UserState {
        return UserState(
            id = id,
            chatId = chatId,
            chatMemberId = chatMemberId
        )
    }

    companion object {
        fun from(
            userState: UserState,
            chat: (ChatId) -> Chat?
        ) : User {
            return User(
                id = userState.id,
                chatId = userState.chatId,
                chatMemberId = userState.chatMemberId,
                chat = chat
            )
        }
    }


}