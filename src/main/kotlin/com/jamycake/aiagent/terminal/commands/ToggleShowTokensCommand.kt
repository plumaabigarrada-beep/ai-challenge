package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.Config

internal class ToggleShowTokensCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        config.showTokens = !config.showTokens
        return "Show tokens ${if (config.showTokens) "enabled" else "disabled"}\n"
    }
}
