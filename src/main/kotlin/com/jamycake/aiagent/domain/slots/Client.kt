package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.Context

internal interface Client {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
        systemPrompt: String = "",
        tools: List<Map<String, Any>>? = null
    ) : ClientResponse

    fun models() : List<String>

    fun close()

}