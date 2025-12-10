# AI Challenge - Project Structure and Description

## Overview

This is a Kotlin-based command-line chatbot application that provides an interactive interface to communicate with multiple AI models. The application acts as a CLI tool that allows users to switch between different AI providers, configure various parameters, and maintain conversation history with token and timing metrics.

## Project Structure

```
ai-challenge/
├── src/
│   ├── main/kotlin/
│   │   ├── Main.kt                         # Entry point with CLI loop
│   │   ├── App.kt                          # Multi-chat manager and app orchestrator
│   │   ├── handleCommands.kt               # Command handler routing logic
│   │   ├── Commands.kt                     # Command definitions and help text
│   │   ├── Command.kt                      # Command parsing logic
│   │   ├── Config.kt                       # Configuration data class
│   │   ├── CoreMessage.kt                  # Message data model
│   │   ├── Colors.kt                       # Terminal color codes
│   │   ├── ApiKeys.kt                      # API credentials
│   │   ├── ErrorHandler.kt                 # Shared error message formatting
│   │   ├── client/
│   │   │   ├── Client.kt                   # Client interface
│   │   │   ├── CoreClientResponse.kt       # API response data model
│   │   │   ├── ChatCompletionModels.kt     # Shared OpenAI-compatible models
│   │   │   └── impl/
│   │   │       ├── PerplexityClient.kt     # Perplexity API implementation
│   │   │       ├── HuggingFaceClient.kt    # HuggingFace API implementation
│   │   │       └── LMStudioClient.kt       # LM Studio local API implementation
│   │   └── chat/
│   │       └── Chat.kt                     # Individual chat logic and state
│   └── test/kotlin/
│       └── CommandTest.kt                  # Unit tests for command parsing
├── build.gradle.kts                        # Gradle build configuration
├── settings.gradle.kts                     # Root project settings
└── gradle/                                 # Gradle wrapper files
```

## Technologies and Dependencies

### Build System
- Kotlin 2.2.0 (JVM-based)
- Gradle 8.14 with Kotlin DSL
- Java 17 (JVM toolchain)

### Core Dependencies
- **Ktor Client 3.0.2**: HTTP client library for API calls
    - ktor-client-core
    - ktor-client-cio
    - ktor-client-content-negotiation
    - ktor-serialization-kotlinx-json
- **Kotlinx Serialization 1.7.3**: JSON serialization/deserialization
- **Coroutines 1.9.0**: Asynchronous programming support
- **JUnit Platform**: Testing framework

## Core Features

### 1. Multi-Chat Support
- Create and manage unlimited independent chat sessions
- Each chat maintains its own conversation history and statistics
- Switch between chats seamlessly with partial ID matching
- Rename chats for better organization
- Delete chats (with safety - cannot delete last chat)
- List all chats with stats (message count, token usage)

### 2. Multi-Client Support
Switch between three AI providers:
- **Perplexity**: Models include sonar-pro, sonar, sonar-reasoning
- **HuggingFace Router**: Supports MiniMax, DeepSeek, Qwen models
- **LM Studio**: Local model runtime with OpenAI-compatible API (localhost:1234)

### 3. Conversation Management
- Maintains conversation history with role tracking (user, assistant, system)
- Clear/reset functionality for conversation history
- System prompt support for customizing AI behavior
- Independent conversation history per chat

### 4. Configuration Options
- Temperature setting (0.0 to <2.0) for response randomness
- Model selection per client
- System prompt customization
- Token display toggle
- Shared configuration across all chats

### 5. Metrics and Analytics
- Token consumption tracking (prompt, response, total)
- Response time measurement (in ms or seconds)
- Statistics display per chat (total tokens, average/total response times)
- Conversation history tracking per chat

### 6. File Reading
- Read text files from disk and send content to AI
- Support for any text-based file format (.txt, .md, .kt, .json, etc.)
- Preview display (first 200 characters) before sending
- Comprehensive error handling (file not found, permissions, etc.)
- File content integrated into conversation history

### 7. Command System
- Command queue using && separator for chaining commands
- Built-in commands for configuration and management
- Chat management commands (create, delete, switch, list, rename)
- Help system with categorized commands listing

## Available Commands

### Configuration Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `--client <name>` | `-cl` | Switch AI provider (perplexity/huggingface/lmstudio) |
| `--models` | `-ls` | List available models for current client |
| `--temperature <value>` | `-t` | Set temperature (0.0 to <2.0) |
| `--model <name>` | `-m` | Set model name |
| `--systemprompt <prompt>` | `-sp` | Set system prompt |
| `--showtokens` | `-st` | Toggle token display |
| `--config` | `-c` | Show current configuration and stats |

### Chat Management Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `--newchat [name]` | `-nc` | Create new chat (optionally with name) |
| `--deletechat <id>` | `-dc` | Delete chat by ID (supports partial ID) |
| `--switchchat <id>` | `-sc` | Switch to chat by ID (supports partial ID) |
| `--chats` | `-lc` | List all chats with stats |
| `--renamechat <name>` | `-rc` | Rename current chat |
| `clear`, `reset` | - | Clear current chat's conversation history |

### File Operations
| Command | Alias | Description |
|---------|-------|-------------|
| `--file <path>` | `-f` | Read file content and send to AI |

### Other Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `--exit`, `--quit` | - | Exit application |
| `help`, `--help` | `-h` | Display help information |

## Architecture

### Design Patterns
- **Multi-Chat Architecture**: Multiple independent chat sessions with shared configuration
- **Client-Server**: CLI communicates with remote AI APIs
- **Interface-based Design**: `Client` interface allows multiple implementations
- **Shared Models**: OpenAI-compatible API models shared across all clients
- **Separation of Concerns**: Chat logic, client implementations, and app orchestration separated

### Package Structure

#### `client/` Package
Contains all client-related code:
- **Client.kt**: Interface defining client contract (`sendMessage`, `models`)
- **CoreClientResponse.kt**: Standardized response model
- **ChatCompletionModels.kt**: Shared OpenAI-compatible models (ChatMessage, ChatCompletionRequest, ChatCompletionResponse, ChatUsage, ChatChoice)
- **impl/**: Client implementations (PerplexityClient, HuggingFaceClient, LMStudioClient)

#### `chat/` Package
Contains chat session logic:
- **Chat.kt**: Individual chat session with conversation history, message handling, and statistics

### Key Components

#### App.kt
Multi-chat manager and application orchestrator:
- Manages multiple chat sessions (create, delete, switch, list, rename)
- Maintains map of chats with current chat tracking
- Coordinates client instances across all chats
- Handles configuration management
- Implements partial ID matching for user convenience
- Delegates message sending to current chat
- Reads and processes text files from disk

#### Chat.kt (chat/Chat.kt)
Individual chat session logic:
- Unique ID (UUID) and customizable name
- Independent conversation history (CoreMessage list)
- Message sending with token and timing tracking
- Statistics calculation (ChatStats: message count, tokens, response times)
- Response formatting with optional token display
- Clear history functionality
- References shared client map and config

#### ChatCompletionModels.kt (client/ChatCompletionModels.kt)
Shared OpenAI-compatible API models:
- **ChatMessage**: role + content
- **ChatCompletionRequest**: model, messages, temperature, stream
- **ChatCompletionResponse**: choices, usage, id, model, created
- **ChatChoice**: message, index, finish_reason
- **ChatUsage**: prompt_tokens, completion_tokens, total_tokens
- **jsonParser**: Shared JSON configuration (ignoreUnknownKeys, prettyPrint)

#### ErrorHandler.kt
Centralized error handling:
- Shared `errorMessage()` function for all clients
- Handles SerializationException, ClientRequestException, ServerResponseException
- Supports optional additional notes (e.g., LM Studio connection guidance)
- Formatted error messages with request info and stack traces

#### PerplexityClient.kt (client/impl/)
Perplexity API wrapper:
- POST requests to `https://api.perplexity.ai/chat/completions`
- Uses shared ChatCompletionModels
- Shared error handling via ErrorHandler
- Token counting from API response

#### HuggingFaceClient.kt (client/impl/)
HuggingFace API wrapper:
- POST requests to `https://router.huggingface.co/v1/chat/completions`
- Uses shared ChatCompletionModels
- Shared error handling via ErrorHandler
- Bearer token authentication

#### LMStudioClient.kt (client/impl/)
LM Studio local API wrapper:
- POST requests to `http://localhost:1234/v1/chat/completions`
- Uses shared ChatCompletionModels
- Shared error handling with custom connection note
- No authentication required (local server)

#### Main.kt
Entry point that provides:
- REPL-style command loop
- Input parsing and command queue processing
- Colored terminal output for UX
- App lifecycle management

#### handleCommands.kt
Command routing function that:
- Matches user input against defined commands
- Extracts command parameters (temperature, model, file paths, chat IDs, etc.)
- Routes commands to appropriate App methods
- Handles chat messages when no command matches
- Returns formatted responses for display

## Configuration

Default configuration:
```kotlin
data class Config(
    var model: String = "sonar-pro",
    var temperature: Double = 0.7,
    var systemPrompt: String = "",
    var clientType: ClientType = PERPLEXITY,
    var showTokens: Boolean = true
)
```

## Build and Execution

- Main class: `org.example.MainKt`
- Jar assembly with shade strategy for fat jar packaging
- HTTP timeout: 3 minutes (180,000 ms) for API calls

## Technical Details

### Async/Await
Uses Kotlin coroutines with `runBlocking` for synchronous CLI execution of async operations.

### Error Handling
Both clients include comprehensive try-catch blocks for:
- Serialization errors
- HTTP client errors (4xx)
- Server errors (5xx)
- Generic exceptions with stack traces

### Output Formatting
- Terminal colors (cyan for user, green for assistant, yellow for info, red for errors)
- Token and timing information in brackets after responses
- Pretty-printed JSON for debugging

### Command Queue
- Supports chaining multiple commands with `&&` separator
- Sequential execution from left to right
- Allows configuration changes followed by queries in one line

### File Reading
- Uses `java.io.File` API for file operations
- UTF-8 encoding by default (Kotlin's `readText()`)
- Validates: path provided, file exists, is a file (not directory), has read permissions
- Shows 200-character preview before sending to AI
- Full file content sent to AI and added to conversation history
- Error handling: file not found, permission denied, IO exceptions
- Example: `--file report.txt` or `-f code.kt && review this`

## Development History

Git commits show progressive development:
- `init`: Initial commit
- Day 2, 3, 6 challenges: Feature additions
- Day 7 challenge: File reading command
- Recent enhancements: tokens showing, time measuring, colors, commands queue, file reading, LM Studio client support
