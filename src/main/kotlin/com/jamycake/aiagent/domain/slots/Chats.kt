package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.chat.Chat

internal interface Chats {

    suspend fun getChat() : Chat

    suspend fun saveChat(chat: Chat)

}