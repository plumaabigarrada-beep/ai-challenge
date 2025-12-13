package com.jamycake.aiagent.domain.core.agent

internal data class AgentState(
    val name: String,
    val config: Config,
    val context: Context
)