package org.example.chat

import chat.ChatUsage

data class ChatMessage(
    val role: String,
    val message: String,
    val usage: ChatUsage?,
)