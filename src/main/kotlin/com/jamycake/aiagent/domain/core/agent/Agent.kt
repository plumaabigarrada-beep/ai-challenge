package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.chat.ChatMessage
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Stats

internal class Agent(
    val id: AgentId = AgentId(),
    var chatMemberId: ChatMemberId? = null,
    val state: AgentState,
    private val clients: Map<ClientType, Client>,
    private val space: Space,
    private val stats: Stats,
    private val onMessageSent: suspend (Agent) -> Unit = {}
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
        val maxToolRounds = 10
        var roundCount = 0

        // Tool execution loop
        while (roundCount < maxToolRounds) {
            roundCount++

            val response = client.sendContext(
                context = state.context,
                temperature = state.config.temperature,
                model = state.config.model,
                tools = state.context.protocol.getToolDefinitions().takeIf { it.isNotEmpty() }
            )

            // Save stats
            stats.save(response.message.id, response.usage)

            if (response.toolCalls.isNotEmpty()) {
                // Tool calls requested
                state.context.addMessage(response.message)

                // Send assistant's content to chat if present
                if (chatMemberId != null && state.chatId != null) {
                    if (response.message.content.isNotEmpty()) {
                        val contentMessage = ChatMessage(
                            role = response.message.role,
                            content = response.message.content,
                            name = state.name,
                            contextMessageId = response.message.id
                        )
                        space.getChat(state.chatId!!)!!.sendMessage(chatMemberId!!, contentMessage)
                    }

                    // Show which tools are being called
                    val toolCallMessage = buildString {
                        append("[Calling tools: ")
                        response.toolCalls.joinToString(", ") { it.name }.let { append(it) }
                        append("]")
                    }

                    val toolNotification = ChatMessage(
                        role = "assistant",
                        content = toolCallMessage,
                        name = state.name,
                        contextMessageId = response.message.id
                    )
                    space.getChat(state.chatId!!)!!.sendMessage(chatMemberId!!, toolNotification)
                }

                // Execute tools in parallel
                val toolResults = state.context.tools.executeToolCalls(
                    response.toolCalls
                )

                // Add tool results to context
                state.context.addToolResults(toolResults)

                // Continue loop to send results back to LLM
            } else {
                // Final response - no more tools
                val newAgentChatMessage = ChatMessage(
                    role = response.message.role,
                    content = response.message.content,
                    name = state.name,
                    contextMessageId = response.message.id
                )

                val sentAgentMessage = response.message.copy(
                    chatMessageId = newAgentChatMessage.id
                )

                state.context.addMessage(sentAgentMessage)

                if (chatMemberId != null && state.chatId != null) {
                    space.getChat(state.chatId!!)!!.sendMessage(
                        chatMemberId!!,
                        newAgentChatMessage
                    )
                }

                onMessageSent(this)
                break  // Exit loop
            }
        }

        if (roundCount >= maxToolRounds) {
            println("Warning: Tool execution limit reached for agent ${state.name}")
        }

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
                state.context.clearMessages()
                state.context.addMessage(compressionResult.first)
                stats.save(compressionResult.first.id, compressionResult.second)
            }
        }
    }

}