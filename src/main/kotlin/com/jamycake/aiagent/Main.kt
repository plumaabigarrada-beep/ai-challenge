package com.jamycake.aiagent

import com.jamycake.aiagent.factories.createApp
import com.jamycake.aiagent.terminal.Colors
import com.jamycake.aiagent.terminal.Command
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {


    val app = createApp()

    println("${Colors.INFO}Chat started! Type 'help' for available commands.${Colors.RESET}\n")
    println("${Colors.INFO}Tip: Use '&&' to chain multiple commands (e.g., '--model sonar && Hello')${Colors.RESET}\n")

    loop@ while (true) {

        print("${Colors.USER}${Colors.BOLD}You:${Colors.RESET} ")
        val userInput = readlnOrNull()?.trim() ?: break

        if (userInput == "") {
            continue
        }

        val inoutCommands = userInput.split("&&")

        inoutCommands.forEach {
            val input = it.trim()

            if (input == "") { return@forEach }

            if (inoutCommands.size > 1) {
                println("${Colors.USER}${Colors.BOLD}You:${Colors.RESET} $input")
            }


            if (input == "--exit" || input == "exit" || input == "quit") {
                println("${Colors.INFO}Goodbye!${Colors.RESET}")
                break
            }


            val command = app.commands.find { it.matches(input) }
            if (command != null) {
                executeCommand(command, input)
            } else {
                val result = app.sendMessageCommand.execute(input)
                println("${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$result${Colors.RESET}\n")
            }

        }


    }

    app.close()
}

private suspend fun executeCommand(command: Command, userInput: String) {
    val args = command.extractValue(userInput)
    val result = command.execute(args)
    println("${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$result${Colors.RESET}\n")
}

