package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.Config

internal class SetModelCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a model name\n"
        }

        config.model = args
        return "Model set to $args\n"
    }
}
