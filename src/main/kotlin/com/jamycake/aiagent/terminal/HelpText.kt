package org.example

const val HELP_TEXT = """
Available commands:

Configuration:
- --client <name>, -cl <name>: Switch client (perplexity/huggingface/lmstudio)
- --models, -ls: List available models for current client
- --temperature <value>, -t <value>: Set temperature (0.0 to 1.0)
- --model <name>, -m <name>: Set model name
- --systemprompt <prompt>, -sp <prompt>: Set system prompt
- --showtokens, -st: Toggle showing token counts after each message
- --autocompress, -ac: Toggle auto-compress (compresses when token limit approached)
- --acthreshold <value>, -act <value>: Set auto-compress threshold (0.5-0.95, e.g., 0.8 = 80%)
- --config, -c: Show current configuration

Chat Management:
- --newchat [name], -nc [name]: Create a new chat (optionally with a name)
- --deletechat <id>, -dc <id>: Delete a chat by ID
- --switchchat <id>, -sc <id>: Switch to a different chat by ID
- --chats, -lc: List all chats
- --renamechat <name>, -rc <name>: Rename current chat
- clear, reset: Clear current chat history
- --compress, -cp: Compress current chat history using AI summarization

File Operations:
- --file <path>, -f <path>: Read file content and send to AI
- --save [directory], -s [directory]: Save current chat to JSON file (default: saved_chats/)

Other:
- --exit, --quit: Exit the application
- --help: Show this help message

Command Queue:
- Use '&&' to chain multiple commands
- Example: '--temperature 0.8 && --model sonar && What is 2+2?'
- Commands execute sequentially from left to right

"""