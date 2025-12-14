package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.domain.space.Space

internal class Agent(
    val id: AgentId = AgentId(),
    var chatMemberId: ChatMemberId? = null,
    val state: AgentState,
    private val clients: Map<ClientType, Client>,
    private val space: Space,
    private val stats: Stats,
) {

    private var clientType: ClientType = state.config.clientType

    private val compressor = ContextCompressor(clients)


    suspend fun updateContext(chatMessage: ChatMessage) {

        compressContextIfNeed()

        val contextMessage = ContextMessage(
            role = chatMessage.role,
            content = chatMessage.content,
        )

        state.context.addMessage(contextMessage)



        val client = clients[clientType]!!
        val (agentMessage, tokensUsage) = client.sendContext(
            context = state.context,
            temperature = state.config.temperature,
            model = state.config.model
        )

        stats.save(contextMessage.id, tokensUsage)


        val newAgnetChatMessage = ChatMessage(
            role = agentMessage.role,
            content = agentMessage.content,
            name = state.name,
            contextMessageId = agentMessage.id
        )

        val sentAgentMessage = agentMessage.copy(chatMessageId = newAgnetChatMessage.id)


        state.context.addMessage(sentAgentMessage)
        if (chatMemberId == null) return
        if (state.chatId == null) return

        space.getChat(state.chatId!!)!!.sendMessage(chatMemberId!!, newAgnetChatMessage)

    }

    private suspend fun compressContextIfNeed() {
        if (state.context.messages.size > state.config.autoCompressMessagesThreshold) {
            val compressionResult = compressor.compress(
                context = state.context,
                threashold = state.config.autoCompressMessagesThreshold,
                model = state.config.compressionModel,
                temperature = state.config.compressionTemperature,
                clientType = state.config.compressionClient
            )

            if (compressionResult != null) {
                state.context.clear()
                state.context.addMessage(compressionResult.first)
                stats.save(compressionResult.first.id, compressionResult.second)
            }
        }
    }

}