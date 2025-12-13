package com.jamycake.aiagent.domain.space

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.agent.AgentId
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.core.user.UserId
import com.jamycake.aiagent.domain.slots.UI

internal class Space(
    private val ui: UI
) {

    private val agents = mutableMapOf<AgentId, Agent>()
    private val chats = mutableMapOf<ChatId, Chat>()
    private val users = mutableMapOf<UserId, User>()

    val allAgents get() = agents.values.toList()
    val allChats get() = chats.values.toList()

    fun addAgent(agent: Agent) {
        agents[agent.id] = agent
    }

    fun getAgent(id: AgentId) : Agent? {
        return agents[id]
    }

    fun addUser(user: User) {
        users[user.id] = user
    }

    fun getUser(userId: UserId) : User? {
        return users[userId]
    }

    fun removeUser(id: UserId) {
        users.remove(id)
    }


    fun addChat(chat: Chat) {
        chats[chat.id] = chat
    }

    fun getChat(id: ChatId) : Chat?{
        return chats[id]
    }

    fun wire(){

        users.values.forEach { user ->
            val chat = chats[user.chatId]
            chat?.addMember(user.chatMemberId) { }
            chat?.addMember(user.chatMemberId) { ui.out(it.content) }
        }

        agents.values.forEach { agent ->
            val chat = chats[agent.state.chatId]
            agent.chatMemberId = chat?.addMember { agent.updateContext(it) }
        }
    }

    fun wireAgentToChat(agentId: AgentId, chatId: ChatId) {
        val agent = agents[agentId] ?: return
        val chat = chats[chatId] ?: return

        // Update agent's chatId
        agent.state.chatId = chatId

        // Wire the agent to the new chat
        agent.chatMemberId = chat.addMember { agent.updateContext(it) }
    }

}