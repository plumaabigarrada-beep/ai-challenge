package org.example.contextsender

import com.jamycake.aiagent.client.Client
import org.example.ClientType
import org.example.chat.ChatMessage
import org.example.context.Context

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