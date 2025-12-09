package org.example

object Commands {
    val exit = Command(listOf("--exit", "--quit"))
    val temperature = Command(listOf("--temperature", "-t"))
    val systemPrompt = Command(listOf("--systemprompt", "-sp"))
    val model = Command(listOf("--model", "-m"))
    val clear = Command(listOf("clear", "reset"))
    val help = Command(listOf("help", "--help", "-h"))
    val config = Command(listOf("--config", "-c"))
    val client = Command(listOf("--client", "-cl"))
    val models = Command(listOf("--models", "-ls"))
}

const val HELP_TEXT = """
Available commands:
- exit, quit: Exit the chat
- --client <name>, -cl <name>: Switch client (perplexity/huggingface)
- --models, -ls: List available models for current client
- --temperature <value>, -t <value>: Set temperature (0.0 to 1.0)
- --model <name>, -m <name>: Set model name
- --systemprompt <prompt>, -sp <prompt>: Set system prompt
- --config, -c: Show current configuration
- clear, reset: Clear conversation history
- help: Show this help message

"""