package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.ClientResponse

internal class ContextCompressor(
    private val clients: Map<ClientType, Client>
) {

    suspend fun compress(
        context: Context,
        threashold: Int,
        model: String,
        clientType: ClientType,
        temperature: Double
    ) : Pair<ContextMessage, TokensUsage>? {

        if (context.messages.size < threashold) return null
        if (context.messages.isEmpty()) return null
        val client = clients[clientType] ?: return null


        val compressionPrompt = CompressionPrompt.createCompressionRequest(context.messages)

        context.clearMessages()




        val response = client.sendContext(
            context = context,
            temperature = temperature,
            model = model,
            systemPrompt = compressionPrompt,
            tools = null
        )

        return Pair(response.message, response.usage)

    }

}