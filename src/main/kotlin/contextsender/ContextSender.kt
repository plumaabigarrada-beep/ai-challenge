package org.example.contextsender

import org.example.chat.ChatMessage
import org.example.context.Context
import client.Client

internal class ContextSender(
    private val client: Client,
) {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String
    ): Pair<Context, ChatMessage> {
        return client.sendContext(context, temperature, model)
    }
}