package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.Config

internal class SetTemperatureCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val temperature = args?.toDoubleOrNull()

        if (temperature == null) {
            return "Please provide a temperature value\n"
        }

        if (temperature in 0.0..< 2.0) {
            config.temperature = temperature
            return "Temperature set to ${config.temperature}\n"
        }

        return "Invalid temperature. Please provide a 0 <= value < 2\n"
    }
}
