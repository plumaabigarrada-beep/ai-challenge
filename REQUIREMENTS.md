# AI Challenge - Requirements

## System Requirements

### Runtime Environment
- Java 17 or higher (JVM)
- Gradle 8.14 or higher
- Operating System: Cross-platform (Linux, macOS, Windows)

### Network Requirements
- Active internet connection for API calls
- Access to the following domains:
  - `api.perplexity.ai`
  - `router.huggingface.co`

## API Requirements

### Authentication
Two API keys required (currently stored in `ApiKeys.kt`):
1. **Perplexity API Key**: For Perplexity client authentication
2. **HuggingFace API Token**: For HuggingFace Router authentication

Both APIs use Bearer token authentication.

## Functional Requirements

### Core Functionality
1. **AI Model Interaction**
   - Send messages to AI models and receive responses
   - Support for multiple AI providers (Perplexity, HuggingFace)
   - Maintain conversation history across multiple turns

2. **Client Management**
   - Switch between different AI clients at runtime
   - List available models for each client
   - Configure model selection

3. **Configuration Management**
   - Adjust temperature parameter (range: 0.0 to <2.0)
   - Set and update system prompts
   - Toggle token display on/off
   - View current configuration

4. **Conversation Control**
   - Clear conversation history
   - Reset to initial state
   - Preserve history across multiple interactions

5. **Metrics Tracking**
   - Track token usage (prompt tokens, completion tokens, total tokens)
   - Measure response times
   - Display cumulative statistics

6. **Command Processing**
   - Parse and execute built-in commands
   - Support command chaining with `&&` separator
   - Provide help information

### User Interface Requirements
1. **Command-Line Interface**
   - REPL-style interaction loop
   - Colored terminal output for better readability
   - Clear visual distinction between user and assistant messages

2. **Output Formatting**
   - Display token usage information (when enabled)
   - Show response timing metrics
   - Provide error messages with context

## Technical Requirements

### Build System
- Gradle-based build system
- Kotlin DSL for build configuration
- Support for creating fat JAR with all dependencies

### Dependencies
- **Ktor Client 3.0.2** or compatible version:
  - ktor-client-core
  - ktor-client-cio (CIO engine)
  - ktor-client-content-negotiation
  - ktor-serialization-kotlinx-json
- **Kotlinx Serialization 1.7.3** or compatible version
- **Kotlinx Coroutines 1.9.0** or compatible version
- **JUnit Platform** for testing

### Programming Language
- Kotlin 2.2.0 or compatible version
- Target JVM platform

## API Compatibility Requirements

### Perplexity API
- Endpoint: `https://api.perplexity.ai/chat/completions`
- HTTP Method: POST
- Authentication: Bearer token
- Request format: JSON with messages array and model parameters
- Response format: JSON with choices array and usage object

### HuggingFace Router API
- Endpoint: `https://router.huggingface.co/v1/chat/completions`
- HTTP Method: POST
- Authentication: Bearer token
- Request format: JSON with messages array and model parameters
- Response format: JSON with choices array and usage object

## Performance Requirements

### Timeout Configuration
- HTTP request timeout: 3 minutes (180,000 ms)
- No specific response time requirements for UI
- Async operations for non-blocking API calls

### Resource Management
- Efficient memory usage for conversation history
- Proper HTTP connection management
- Graceful error handling without crashes

## Error Handling Requirements

### Error Categories
1. **Serialization Errors**: Handle JSON parsing failures
2. **Client Errors (4xx)**: Handle invalid requests and authentication failures
3. **Server Errors (5xx)**: Handle API server issues
4. **Network Errors**: Handle connection failures and timeouts

### Error Reporting
- Display user-friendly error messages
- Include stack traces for debugging (when appropriate)
- Maintain application stability after errors

## Testing Requirements

### Unit Testing
- Test framework: JUnit Platform
- Test command parsing logic
- Verify command recognition and parsing

## Security Considerations

### API Key Management
- API keys stored in `ApiKeys.kt`
- Bearer token authentication for all API calls
- Note: Current implementation has API keys in source code (not production-safe)

### Network Security
- HTTPS connections to all API endpoints
- No local data persistence of sensitive information

## Command Specification

### Required Commands
| Command | Aliases | Parameters | Description |
|---------|---------|------------|-------------|
| `--exit` | `--quit` | None | Exit application |
| `--client` | `-cl` | `<name>` | Switch AI client |
| `--models` | `-ls` | None | List available models |
| `--temperature` | `-t` | `<value>` | Set temperature (0.0 to <2.0) |
| `--model` | `-m` | `<name>` | Select model |
| `--systemprompt` | `-sp` | `<text>` | Set system prompt |
| `--showtokens` | `-st` | None | Toggle token display |
| `--config` | `-c` | None | Show configuration |
| `clear` | `reset` | None | Clear history |
| `help` | None | None | Display help |

### Command Chaining
- Support `&&` separator for sequential command execution
- Process commands from left to right
- Execute each command before moving to next

## Extensibility Requirements

### Design for Extension
- Interface-based client design for adding new AI providers
- Modular command system for adding new commands
- Configurable model lists per client
- Flexible message format conversion between core and API-specific formats
