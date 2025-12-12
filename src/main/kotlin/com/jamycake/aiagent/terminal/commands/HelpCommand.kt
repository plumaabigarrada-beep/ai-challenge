package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.HELP_TEXT

internal class HelpCommand(
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        return HELP_TEXT
    }
}
