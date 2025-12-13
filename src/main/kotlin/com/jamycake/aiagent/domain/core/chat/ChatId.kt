package com.jamycake.aiagent.domain.core.chat

import java.util.*

data class ChatId(
    val value: String = UUID.randomUUID().toString()
) {
    companion object {
        fun empty() : ChatId {
            return ChatId("")
        }
    }
}