package org.example

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val app = App()

    println("${Colors.INFO}Chat started! Type 'help' for available commands.${Colors.RESET}\n")

    while (true) {
        print("${Colors.USER}${Colors.BOLD}You:${Colors.RESET} ")
        val userInput = readlnOrNull()?.trim() ?: break

        if (userInput.isEmpty()) continue
        if (Commands.exit.matches(userInput)) {
            println("${Colors.INFO}Goodbye!${Colors.RESET}")
            break
        }

        val response = handleCommands(userInput, app)

        println(response)
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

    else -> {
        val response = app.sendMessage(userInput)
        "\n${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$response${Colors.RESET}\n"
    }
}
