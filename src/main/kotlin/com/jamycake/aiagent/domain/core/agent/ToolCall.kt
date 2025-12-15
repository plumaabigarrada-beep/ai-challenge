package com.jamycake.aiagent.domain.core.agent

import kotlinx.serialization.Serializable

@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, String>
)
