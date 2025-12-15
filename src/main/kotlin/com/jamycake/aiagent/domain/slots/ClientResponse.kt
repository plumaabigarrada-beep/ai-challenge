package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.ContextMessage
import com.jamycake.aiagent.domain.core.agent.TokensUsage
import com.jamycake.aiagent.domain.core.agent.ToolCall

internal data class ClientResponse(
    val message: ContextMessage,
    val usage: TokensUsage,
    val toolCalls: List<ToolCall> = emptyList()
)
