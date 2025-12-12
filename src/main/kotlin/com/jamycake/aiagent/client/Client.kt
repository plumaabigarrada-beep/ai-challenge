package com.jamycake.aiagent.client

import com.jamycake.aiagent.context.Context
import com.jamycake.aiagent.chat.ChatMessage

internal interface Client {

    suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
    ) : ChatMessage

    fun models() : List<String>

    fun close()

}