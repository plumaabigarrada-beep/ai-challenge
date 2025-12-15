package com.jamycake.aiagent.domain.core.agent

import java.util.*

internal data class ContextMessage (
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val chatMessageId: String? = null,
    val toolCallId: String? = null,
    val toolCalls: List<ToolCall>? = null
)