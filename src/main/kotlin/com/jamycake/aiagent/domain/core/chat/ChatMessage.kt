package com.jamycake.aiagent.domain.core.chat

import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val content: String,
    val contextMessageId: String? = null
)