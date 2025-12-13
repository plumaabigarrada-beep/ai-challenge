package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.slots.Chats

internal class ChatsImpl : Chats {

    override suspend fun getChat(): Chat {
        val chatId = ChatId.empty()
        val chat = Chat(id = chatId)
        return chat
    }

    override suspend fun saveChat(chat: Chat) {

    }
}