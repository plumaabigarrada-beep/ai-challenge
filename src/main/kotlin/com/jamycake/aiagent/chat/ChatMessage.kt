package com.jamycake.aiagent.chat

data class ChatMessage(
    val role: String,
    val message: String,
    val usage: ChatUsage?,
)