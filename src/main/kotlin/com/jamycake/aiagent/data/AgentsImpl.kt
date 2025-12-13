package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.agent.*
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.domain.space.Space
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class AgentsImpl(
    private val clients: Map<ClientType, Client>,
    private val space: Space,
    private val stats: Stats,
    private val storagePath: String = "agents/"
) : Agents {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun get(): List<Agent> {
        val agentFolder = File(storagePath)
        val listFiles = agentFolder.listFiles()
        if (!agentFolder.exists() || listFiles.isEmpty()) {
            return emptyList()
        }

        return listFiles.map { file ->
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
                context = context,
                chatId = ChatId.empty()
            )

            val chatMemberId = ChatMemberId(savedData.id)

            Agent(
                chatMemberId = chatMemberId,
                state = agentState,
                clients = clients,
                stats = stats,
                space = space
            )
        }


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

        // Ensure the directory exists
        val agentFolder = File(storagePath)
        if (!agentFolder.exists()) {
            agentFolder.mkdirs()
        }

        // Write to a specific file using agent's ID
        File(storagePath, "${agent.id.value}.json").writeText(jsonString)
    }

    override suspend fun new() : Agent {
        return defauldAgent(
            clients = clients,
            space = space,
            stats = stats
        )
    }

    private fun defauldAgent(
        clients: Map<ClientType, Client>,
        space: Space,
        stats: Stats
    ): Agent {
        val agentState = AgentState(
            name = "",
            config = Config(temperature = 0.7),
            context = Context(messages = emptyList()),
            chatId = ChatId.empty()
        )

        val agent = Agent(
            state = agentState,
            clients = clients,
            space = space,
            stats = stats,
        )
        return agent
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
