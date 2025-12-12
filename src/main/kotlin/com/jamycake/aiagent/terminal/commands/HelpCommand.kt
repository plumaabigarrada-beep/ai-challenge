package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import org.example.HELP_TEXT

internal class HelpCommand(
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        return HELP_TEXT
    }
}
