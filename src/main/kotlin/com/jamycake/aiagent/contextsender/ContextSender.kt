package com.jamycake.aiagent.contextsender

import com.jamycake.aiagent.client.Client
import com.jamycake.aiagent.chat.ClientType
import com.jamycake.aiagent.chat.ChatMessage
import com.jamycake.aiagent.context.Context

internal class ContextSender(
    private val clients: Map<ClientType, Client>,
) {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
        clientType: ClientType,
    ): ChatMessage{
        return clients[clientType]?.sendContext(context, temperature, model) ?: throw IllegalStateException("Client $clientType not found")
    }
}