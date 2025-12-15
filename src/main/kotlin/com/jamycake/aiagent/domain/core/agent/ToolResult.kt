package com.jamycake.aiagent.domain.core.agent

data class ToolResult(
    val callId: String,
    val output: String,
    val error: String? = null,
    val isSuccess: Boolean = true
)
