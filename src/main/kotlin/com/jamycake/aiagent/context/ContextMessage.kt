package org.example.context

import com.jamycake.aiagent.chat.ChatUsage

data class ContextMessage(
    val role: String,
    val content: String,
    val usage: ChatUsage? = null,
)