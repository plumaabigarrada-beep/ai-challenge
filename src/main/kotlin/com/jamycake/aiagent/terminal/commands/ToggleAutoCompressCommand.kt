package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import org.example.Config

internal class ToggleAutoCompressCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        config.autoCompressEnabled = !config.autoCompressEnabled
        return "Auto-compress ${if (config.autoCompressEnabled) "enabled" else "disabled"}\n"
    }
}
