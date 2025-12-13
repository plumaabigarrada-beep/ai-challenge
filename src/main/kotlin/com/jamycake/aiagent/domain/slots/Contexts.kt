package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.AgentId
import com.jamycake.aiagent.domain.core.agent.Context

internal interface Contexts {

    suspend fun save(agentId: AgentId, context: Context)

    suspend fun restore(agentId: AgentId) : Context

}