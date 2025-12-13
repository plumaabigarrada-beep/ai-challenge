package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.Agent

internal interface Agents {

    suspend fun get(): List<Agent>

    suspend fun save(agents: Agent)

    suspend fun new() : Agent
}