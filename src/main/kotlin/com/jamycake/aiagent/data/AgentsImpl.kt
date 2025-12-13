package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.agent.*
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Stats
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class AgentsImpl(
    private val clients: Map<ClientType, Client>,
    private val chats: Map<String, Chat>,
    private val stats: Stats,
    private val storagePath: String = "agent-state.json"
) : Agents {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun get(): Agent {
        val file = File(storagePath)
        if (!file.exists()) {
            throw IllegalStateException("Agent not found. Please save an agent first.")
        }

        val savedData = json.decodeFromString<SavedAgentData>(file.readText())

        val context = Context(
            messages = savedData.state.contextMessages.map { msg ->
                ContextMessage(
                    id = msg.id,
                    role = msg.role,
                    content = msg.content,
                    chatMessageId = msg.chatMessageId
                )
            }
        )

        val agentState = AgentState(
            name = savedData.state.name,
            config = savedData.state.config,
            context = context
        )

        val chatMemberId = ChatMemberId(savedData.id)

        return Agent(
            chatMemberId = chatMemberId,
            state = agentState,
            clients = clients,
            chats = chats,
            stats = stats
        )
    }

    override suspend fun save(agent: Agent) {

        val agentState = agent.state

        println(agent.state.config)

        val savedState = SavedAgentState(
            name = agentState.name,
            config = agentState.config,
            contextMessages = agentState.context.messages.map { msg ->
                SavedContextMessage(
                    id = msg.id,
                    role = msg.role,
                    content = msg.content,
                    chatMessageId = msg.chatMessageId
                )
            }
        )

        val savedData = SavedAgentData(
            id = agent.id.value,
            chatMemberId = agent.chatMemberId.value,
            state = savedState
        )

        val jsonString = json.encodeToString(savedData)
        File(storagePath).writeText(jsonString)
    }

    @Serializable
    private data class SavedAgentData(
        val id: String,
        val chatMemberId: String,
        val state: SavedAgentState
    )

    @Serializable
    private data class SavedAgentState(
        val name: String,
        val config: Config,
        val contextMessages: List<SavedContextMessage>
    )

    @Serializable
    private data class SavedContextMessage(
        val id: String,
        val role: String,
        val content: String,
        val chatMessageId: String? = null
    )
}
