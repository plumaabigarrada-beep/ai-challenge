package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.tools.Tools
import com.jamycake.aiagent.domain.slots.Agents
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.domain.core.Space
import com.jamycake.aiagent.terminal.Command

internal class CurrentAgentCommand(
    private val space: Space,
    private val agents: Agents,
    private val tools: Tools,
    private val ui: UI
) : Command(values = listOf("--current-agent")) {

    override suspend fun execute(args: String?) {
        val agent = space.currentAgent
        if (agent == null) {
            ui.out("No agent selected. Use --select-agent first")
            return
        }

        when (args?.trim()?.lowercase()) {
            null, "" -> showAgentInfo()
            "messages" -> showMessages()
            "tools" -> showTools()
            else -> {
                val parts = args.split(" ", limit = 2)
                when (parts[0].lowercase()) {
                    "add-tool" -> {
                        if (parts.size < 2) {
                            ui.out("Tool name or ID must be provided")
                            return
                        }
                        addTool(parts[1])
                    }
                    "remove-tool" -> {
                        if (parts.size < 2) {
                            ui.out("Tool name must be provided")
                            return
                        }
                        removeTool(parts[1])
                    }
                    else -> ui.out("Unknown subcommand: ${parts[0]}")
                }
            }
        }
    }

    private fun showAgentInfo() {
        val agent = space.currentAgent!!
        val output = buildString {
            appendLine("Current Agent:")
            appendLine("-------------")
            appendLine("ID: ${agent.id.value}")
            appendLine("Name: ${agent.state.name}")
            appendLine("Model: ${agent.state.config.model}")
            appendLine("Chat ID: ${agent.state.chatId?.value ?: "Not wired"}")
            appendLine("Messages: ${agent.state.context.messages.size}")
            appendLine("Tools: ${agent.state.context.protocol.getTools().size}")
            appendLine("-------------")
        }
        ui.out(output)
    }

    private fun showMessages() {
        val agent = space.currentAgent!!
        val messages = agent.state.context.messages

        if (messages.isEmpty()) {
            ui.out("No messages in context")
            return
        }

        val output = buildString {
            appendLine("Messages (${messages.size}):")
            appendLine("-------------")
            messages.forEach { message ->
                val firstLine = message.content.lines().firstOrNull() ?: ""
                val preview = if (firstLine.length > 60) {
                    firstLine.substring(0, 60) + "..."
                } else {
                    firstLine
                }
                appendLine("[${message.role}] $preview")
            }
            appendLine("-------------")
        }
        ui.out(output)
    }

    private fun showTools() {
        val agent = space.currentAgent!!
        val agentTools = agent.state.context.protocol.getTools()

        if (agentTools.isEmpty()) {
            ui.out("No tools registered for this agent")
            return
        }

        val output = buildString {
            appendLine("Agent Tools (${agentTools.size}):")
            appendLine("-------------")
            agentTools.forEach { tool ->
                appendLine("${tool.name}: ${tool.description}")
            }
            appendLine("-------------")
        }
        ui.out(output)
    }

    private suspend fun addTool(toolName: String) {
        val agent = space.currentAgent!!
        val tool = tools.getTool(toolName)

        if (tool == null) {
            ui.out("Tool not found: $toolName")
            return
        }

        agent.state.context.protocol.registerTool(tool)
        agents.save(agent)
        ui.out("Tool '${tool.name}' added to agent ${agent.state.name}")
    }

    private suspend fun removeTool(toolName: String) {
        val agent = space.currentAgent!!
        agent.state.context.protocol.removeTool(toolName)
        agents.save(agent)
        ui.out("Tool '$toolName' removed from agent ${agent.state.name}")
    }
}
