package com.jamycake.aiagent.chat

data class ChatStats(
    val messageCount: Int,
    val totalTokens: Int,
    val avgResponseTime: Long,
    val totalResponseTime: Long
)