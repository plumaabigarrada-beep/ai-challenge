package org.example

object Commands {
    val exit = Command(listOf("exit", "quit"))
    val temperature = Command(listOf("--temperature", "-t"))
    val systemPrompt = Command(listOf("--systemprompt", "-sp"))
    val model = Command(listOf("--model", "-m"))
    val clear = Command(listOf("clear", "reset"))
    val help = Command(listOf("help", "--help", "-h"))
}

const val HELP_TEXT = """
Available commands:
- exit, quit: Exit the chat
- --temperature <value>, -t <value>: Set temperature (0.0 to 1.0)
- --model <name>, -m <name>: Set model name
- --systemprompt <prompt>, -sp <prompt>: Set system prompt
- clear, reset: Clear conversation history
- help: Show this help message

"""