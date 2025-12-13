package com.jamycake.aiagent.app.commands.help

import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class HelpCommand(
    private val allCommands: () -> List<Command>,
    private val ui: UI
) : Command(listOf("--help", "-h")) {

    override suspend fun execute(args: String?) {
        val commands = allCommands()

        val output = buildString {
            appendLine("Available Commands:")
            appendLine("===================")
            appendLine()

            // Group commands by their primary value (first value)
            val commandGroups = mutableMapOf<String, MutableList<String>>()

            commands.forEach { command ->
                val primary = command.values.firstOrNull() ?: return@forEach
                val aliases = command.values.drop(1)

                if (commandGroups.containsKey(primary)) {
                    // Skip duplicates
                    return@forEach
                }

                commandGroups[primary] = aliases.toMutableList()
            }

            // Sort commands alphabetically
            commandGroups.keys.sorted().forEach { primary ->
                val aliases = commandGroups[primary]!!
                if (aliases.isNotEmpty()) {
                    appendLine("  $primary (${aliases.joinToString(", ")})")
                } else {
                    appendLine("  $primary")
                }
            }

            appendLine()
            appendLine("===================")
            appendLine("Total: ${commandGroups.size} commands")
        }

        ui.out(output)
    }
}
