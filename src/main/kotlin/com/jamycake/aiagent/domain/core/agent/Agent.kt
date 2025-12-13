package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Stats

internal class Agent(
    val chatMemberId: ChatMemberId = ChatMemberId(),
    private val name: String = "",
    private val config: Config,
    private val context: Context,
    private val clients: Map<ClientType, Client>,
    private val chats: Map<String, Chat>,
    private val stats: Stats,
    clientType: ClientType,
) {

    private var chatId = chats.keys.first()
    private var clientType: ClientType = clientType


    suspend fun updateContext(chatMessage: ChatMessage) {

        val contextMessage = ContextMessage(
            role = chatMessage.role,
            content = chatMessage.content,
        )

        context.addMessage(contextMessage)

        val client = clients[clientType]!!
        val (newContextMessage, tokensUsage) = client.sendContext(
            context = context,
            temperature = config.temperature,
            model = config.model
        )

        stats.save(contextMessage.id, tokensUsage)


        val newChatMessage = ChatMessage(
            role = newContextMessage.role,
            content = newContextMessage.content,
            contextMessageId = newContextMessage.id
        )

        val sentContextMessage = newContextMessage.copy(chatMessageId = newChatMessage.id)


        context.addMessage(sentContextMessage)
        chats[chatId]!!.sendMessage(chatMemberId, newChatMessage)

    }

}