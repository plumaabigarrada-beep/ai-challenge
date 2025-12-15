package com.jamycake.aiagent.app.commands.agents

import com.jamycake.aiagent.domain.core.tools.Tools
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class ToolsCommand(
    private val tools: Tools,
    private val ui: UI
) : Command(values = listOf("--tools")) {

    override suspend fun execute(args: String?) {
        val allTools = tools.getAllTools()

        if (allTools.isEmpty()) {
            ui.out("No tools available")
            return
        }

        val output = buildString {
            appendLine("Available Tools (${allTools.size}):")
            appendLine("-------------")
            allTools.forEach { tool ->
                appendLine("${tool.name}:")
                appendLine("  Description: ${tool.description}")
            }
            appendLine("-------------")
        }
        ui.out(output)
    }
}
