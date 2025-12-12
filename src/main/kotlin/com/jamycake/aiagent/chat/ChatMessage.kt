package org.example.chat

import com.jamycake.aiagent.chat.ChatUsage

data class ChatMessage(
    val role: String,
    val message: String,
    val usage: ChatUsage?,
)