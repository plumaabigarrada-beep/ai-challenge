package com.jamycake.aiagent.terminal

internal class Terminal(
    private val onNoCommands: suspend (input: String) -> Unit,
    private val commands: List<Command>
) {

    suspend fun run() {

        while (true) {
            print("> ")
            val line = readln().trim()
            if (line == "--exit") break
            if (line.isEmpty()) continue


            val command = commands.find { it.matches(line) }

            command?.let {
                val args = it.extractValue(line)
                command.execute(args)
            }

            if (line.isNotEmpty() && command == null) {
                onNoCommands(line)
            }
        }

    }

}