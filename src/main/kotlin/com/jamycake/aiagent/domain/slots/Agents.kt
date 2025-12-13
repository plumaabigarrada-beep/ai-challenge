package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.Agent

internal interface Agents {

    suspend fun getAll() : List<Agent>
}