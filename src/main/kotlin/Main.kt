package org.example

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val app = App()

    println("${Colors.INFO}Chat started! Type 'help' for available commands.${Colors.RESET}\n")
    println("${Colors.INFO}Tip: Use '&&' to chain multiple commands (e.g., '--model sonar && Hello')${Colors.RESET}\n")

    while (true) {
        print("${Colors.USER}${Colors.BOLD}You:${Colors.RESET} ")
        val userInput = readlnOrNull()?.trim() ?: break

        if (userInput.isEmpty()) continue
        if (Commands.exit.matches(userInput)) {
            println("${Colors.INFO}Goodbye!${Colors.RESET}")
            break
        }

        // Split input by && to create command queue
        val commandQueue = userInput.split("&&").map { it.trim() }.filter { it.isNotEmpty() }

        for ((index, command) in commandQueue.withIndex()) {
            if (Commands.exit.matches(command)) {
                println("${Colors.INFO}Goodbye!${Colors.RESET}")
                app.exit()
                return@runBlocking
            }

            val response = handleCommands(command, app)

            println(response)

            // Add spacing between commands if there are multiple
            if (commandQueue.size > 1 && index < commandQueue.size - 1) {
                println("${Colors.INFO}$command${Colors.RESET}")
            }
        }
    }

    app.exit()
}

private suspend fun handleCommands(userInput: String, app: App): String = when {

    Commands.help.matches(userInput) -> {
        HELP_TEXT
    }

    Commands.temperature.matches(userInput) -> {
        val temp = Commands.temperature.extractDoubleValue(userInput)
        app.setTemperature(temp)
    }

    Commands.model.matches(userInput) -> {
        val modelName = Commands.model.extractValue(userInput)
        app.setModel(modelName)
    }

    Commands.systemPrompt.matches(userInput) -> {
        val prompt = Commands.systemPrompt.extractValue(userInput)
        app.setSystemPrompt(prompt)
    }

    Commands.clear.matches(userInput) -> {
        app.clearHistory()
    }

    Commands.config.matches(userInput) -> {
        app.getConfig()
    }

    Commands.client.matches(userInput) -> {
        val clientName = Commands.client.extractValue(userInput)
        app.setClient(clientName)
    }

    Commands.models.matches(userInput) -> {
        app.listModels()
    }

    Commands.showTokens.matches(userInput) -> {
        app.toggleShowTokens()
    }

    Commands.file.matches(userInput) -> {
        val filePath = Commands.file.extractValue(userInput)
        app.readAndSendFile(filePath)
    }

    else -> {
        val response = app.sendMessage(userInput)
        "\n${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$response${Colors.RESET}\n"
    }
}
