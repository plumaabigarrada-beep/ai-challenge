package org.example

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val app = App()

    println("Chat started! Type 'help' for available commands.\n")

    while (true) {
        print("You: ")
        val userInput = readlnOrNull()?.trim() ?: break

        if (userInput.isEmpty()) continue
        if (Commands.exit.matches(userInput)) {
            println("Goodbye!")
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

    else -> {
        val response = app.sendMessage(userInput)
        "\nAssistant: $response\n"
    }
}
