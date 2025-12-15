package com.jamycake.aiagent.app.commands.space

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.agent.AgentId
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class WireAgentToChatCommand(
    private val allAgents: () -> List<Agent>,
    private val allChats: () -> List<Chat>,
    private val space: Space,
    private val ui: UI
) : Command(listOf("--wire")) {

    override suspend fun execute(args: String?) {
        if (args.isNullOrBlank()) {
            ui.out("Usage: --wire [agent-name-or-id] [chat-id]")
            return
        }

        val parts = args.trim().split(Regex("\\s+"), limit = 2)
        if (parts.size < 2) {
            ui.out("Usage: --wire [agent-name-or-id] [chat-id]")
            return
        }

        val agentIdentifier = parts[0]
        val chatIdentifier = parts[1]

        // Find agent by name or ID
        val agent = allAgents().find {
            it.state.name == agentIdentifier || it.id.value == agentIdentifier
        }

        if (agent == null) {
            ui.out("Agent not found: $agentIdentifier")
            return
        }

        // Find chat by ID
        val chat = allChats().find { it.id.value == chatIdentifier }

        if (chat == null) {
            ui.out("Chat not found: $chatIdentifier")
            return
        }

        // Wire the agent to the chat
        space.wireAgentToChat(agent.id, chat.id)

        ui.out("Successfully wired agent '${agent.state.name}' (${agent.id.value}) to chat ${chat.id.value}")
    }
}
