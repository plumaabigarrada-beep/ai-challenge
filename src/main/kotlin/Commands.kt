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
    val showTokens = Command(listOf("--showtokens", "-st"))
    val file = Command(listOf("--file", "-f"))
}

const val HELP_TEXT = """
Available commands:
- exit, quit: Exit the chat
- --client <name>, -cl <name>: Switch client (perplexity/huggingface)
- --models, -ls: List available models for current client
- --temperature <value>, -t <value>: Set temperature (0.0 to 1.0)
- --model <name>, -m <name>: Set model name
- --systemprompt <prompt>, -sp <prompt>: Set system prompt
- --showtokens, -st: Toggle showing token counts after each message
- --config, -c: Show current configuration
- --file <path>, -f <path>: Read file content and send to AI
- clear, reset: Clear conversation history
- help: Show this help message

Command Queue:
- Use '&&' to chain multiple commands
- Example: '--temperature 0.8 && --model sonar && What is 2+2?'
- Commands execute sequentially from left to right

"""