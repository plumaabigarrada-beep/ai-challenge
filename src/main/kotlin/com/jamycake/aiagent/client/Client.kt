package com.jamycake.aiagent.client

import org.example.chat.ChatMessage
import org.example.context.Context

internal interface Client {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
    ) : ChatMessage

    fun models() : List<String>

    fun close()

}