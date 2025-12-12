package com.jamycake.aiagent.chat

data class CoreMessage(
    val role: String,
    val content: String,
    val tokens: Int? = null,
    val durationMs: Long? = null
)