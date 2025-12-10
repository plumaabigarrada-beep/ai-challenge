# AI Challenge - Project Structure and Description

## Overview

This is a Kotlin-based command-line chatbot application that provides an interactive interface to communicate with multiple AI models. The application acts as a CLI tool that allows users to switch between different AI providers, configure various parameters, and maintain conversation history with token and timing metrics.

## Project Structure

```
ai-challenge/
├── src/
│   ├── main/kotlin/
│   │   ├── Main.kt                 # Entry point with CLI loop (97 lines)
│   │   ├── App.kt                  # Core application logic (249 lines)
│   │   ├── handleCommands.kt       # Command handler routing logic (57 lines)
│   │   ├── Client.kt               # Client interface (12 lines)
│   │   ├── PerplexityClient.kt     # Perplexity API implementation (171 lines)
│   │   ├── HuggingFaceClient.kt    # HuggingFace API implementation (166 lines)
│   │   ├── Config.kt               # Configuration data class (13 lines)
│   │   ├── Commands.kt             # Command definitions (36 lines)
│   │   ├── Command.kt              # Command parsing logic (25 lines)
│   │   ├── CoreMessage.kt          # Message data model (7 lines)
│   │   ├── CoreClientResponse.kt   # API response data model (6 lines)
│   │   ├── Colors.kt               # Terminal color codes (10 lines)
│   │   └── ApiKeys.kt              # API credentials (3 lines)
│   └── test/kotlin/
│       └── CommandTest.kt          # Unit tests for command parsing
├── build.gradle.kts                # Gradle build configuration
├── settings.gradle.kts             # Root project settings
└── gradle/                         # Gradle wrapper files
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

### 1. Multi-Client Support
Switch between two AI providers:
- **Perplexity**: Models include sonar-pro, sonar, sonar-reasoning
- **HuggingFace Router**: Supports MiniMax, DeepSeek, Qwen models

### 2. Conversation Management
- Maintains conversation history with role tracking (user, assistant, system)
- Clear/reset functionality for conversation history
- System prompt support for customizing AI behavior

### 3. Configuration Options
- Temperature setting (0.0 to <2.0) for response randomness
- Model selection per client
- System prompt customization
- Token display toggle

### 4. Metrics and Analytics
- Token consumption tracking (prompt, response, total)
- Response time measurement (in ms or seconds)
- Statistics display (total tokens, average/total response times)

### 5. File Reading
- Read text files from disk and send content to AI
- Support for any text-based file format (.txt, .md, .kt, .json, etc.)
- Preview display (first 200 characters) before sending
- Comprehensive error handling (file not found, permissions, etc.)
- File content integrated into conversation history

### 6. Command System
- Command queue using && separator for chaining commands
- Built-in commands for configuration and management
- Help system with available commands listing

## Available Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `--exit`, `--quit` | - | Exit application |
| `--client <name>` | `-cl` | Switch AI provider |
| `--models` | `-ls` | List available models |
| `--temperature <value>` | `-t` | Set temperature |
| `--model <name>` | `-m` | Set model |
| `--systemprompt <prompt>` | `-sp` | Set system prompt |
| `--showtokens` | `-st` | Toggle token display |
| `--config` | `-c` | Show current configuration |
| `--file <path>` | `-f` | Read file content and send to AI |
| `clear`, `reset` | - | Clear conversation history |
| `help` | - | Display help information |

## Architecture

### Design Pattern
- **Client-Server**: CLI communicates with remote AI APIs
- **Interface-based Design**: `Client` interface allows multiple implementations
- **Data Class Models**: Separate data classes for API-specific message formats

### Key Components

#### App.kt
Central business logic that:
- Manages conversation history
- Handles message sending and response formatting
- Manages configuration and client switching
- Compiles metrics (tokens, response times)
- Reads and processes text files from disk

#### PerplexityClient.kt
Perplexity API wrapper:
- POST requests to `https://api.perplexity.ai/chat/completions`
- Comprehensive error handling (serialization, HTTP 4xx/5xx errors)
- Token counting from API response

#### HuggingFaceClient.kt
HuggingFace API wrapper:
- POST requests to `https://router.huggingface.co/v1/chat/completions`
- Similar error handling and token tracking
- Bearer token authentication

#### Main.kt
Entry point that provides:
- REPL-style command loop
- Input parsing and command queue processing
- Colored terminal output for UX

#### handleCommands.kt
Command routing function that:
- Matches user input against defined commands
- Extracts command parameters (temperature, model, file paths, etc.)
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
- Recent enhancements: tokens showing, time measuring, colors, commands queue, file reading
