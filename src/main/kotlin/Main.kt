package org.example

import handleCommands
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

