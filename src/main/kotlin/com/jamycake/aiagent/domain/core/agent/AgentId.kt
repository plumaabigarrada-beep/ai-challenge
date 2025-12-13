package com.jamycake.aiagent.domain.core.agent

import java.util.*

internal data class AgentId(
    val value: String = UUID.randomUUID().toString()
)