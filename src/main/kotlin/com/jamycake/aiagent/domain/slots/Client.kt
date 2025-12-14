package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.Context
import com.jamycake.aiagent.domain.core.agent.ContextMessage
import com.jamycake.aiagent.domain.core.agent.TokensUsage

internal interface Client {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
        systemPrmpt: String = "",
    ) : Pair<ContextMessage, TokensUsage>

    fun models() : List<String>

    fun close()

}