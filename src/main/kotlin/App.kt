package org.example

class App {

    private val config = Config()
    private val perplexityClient = PerplexityClient()
    private val huggingFaceClient = HuggingFaceClient()
    private val conversationHistory = mutableListOf<CoreMessage>()


    suspend fun sendMessage(text: String) : String {

        conversationHistory.add(CoreMessage(role = "user", content = text))

        val history = if (config.systemPrompt.isEmpty()) {
            conversationHistory
        } else {
            listOf(CoreMessage(role = "system", content = config.systemPrompt)) + conversationHistory
        }

        val client = client()

        // Measure response time
        val startTime = System.currentTimeMillis()
        val response = client.sendMessage(
            conversationHistory = history,
            temperature = config.temperature,
            model = config.model
        )
        val duration = System.currentTimeMillis() - startTime

        if (response.content.isNotEmpty()) {
            // Update the last user message with prompt tokens
            if (response.promptTokens != null && conversationHistory.isNotEmpty()) {
                val lastIndex = conversationHistory.lastIndex
                conversationHistory[lastIndex] = conversationHistory[lastIndex].copy(
                    tokens = response.promptTokens
                )
            }

            // Add assistant response with response tokens and duration
            conversationHistory.add(
                CoreMessage(
                    role = "assistant",
                    content = response.content,
                    tokens = response.responseTokens,
                    durationMs = duration
                )
            )

            // Build response with optional token and time information
            return if (config.showTokens) {
                buildString {
                    append(response.content)
                    append("\n\n[")

                    // Add token information if available
                    if (response.promptTokens != null || response.responseTokens != null) {
                        append("Tokens: ")
                        if (response.promptTokens != null) append("prompt=${response.promptTokens}")
                        if (response.promptTokens != null && response.responseTokens != null) append(", ")
                        if (response.responseTokens != null) append("response=${response.responseTokens}")
                        val total = (response.promptTokens ?: 0) + (response.responseTokens ?: 0)
                        append(", total=$total")
                        append(" | ")
                    }

                    // Add time information
                    append("Time: ")
                    if (duration < 1000) {
                        append("${duration}ms")
                    } else {
                        val seconds = duration / 1000.0
                        append(String.format("%.2fs", seconds))
                    }
                    append("]")
                }
            } else {
                response.content
            }
        } else {
            conversationHistory.removeLastOrNull()
            return ""
        }
    }


    fun exit() {
        perplexityClient.close()
        huggingFaceClient.close()
    }

    fun setTemperature(temperature: Double?) : String {
        if (temperature == null) return "Provide temperature value"
        if (temperature in 0.0..< 2.0) {
            config.temperature = temperature
            return "Temperature set to ${config.temperature}\n"
        }

        return "Invalid temperature. Please provide a 0 <= value < 2\n"
    }

    fun setSystemPrompt(prompt: String?): String {
        if (prompt.isNullOrEmpty()) {
            return "Please provide a system prompt\n"
        }

        config.systemPrompt = prompt

        return "System prompt set.\n"
    }

    fun setModel(model: String?): String {
        if (model.isNullOrEmpty()) {
            return "Please provide a model name\n"
        }

        config.model = model
        return "Model set to $model\n"
    }

    fun clearHistory(): String {
        conversationHistory.clear()
        if (config.systemPrompt.isNotEmpty()) {
            conversationHistory.add(CoreMessage(role = "system", content = config.systemPrompt))
        }
        return "Conversation history cleared.\n"
    }

    fun getConfig(): String {
        val totalTokens = conversationHistory.sumOf { it.tokens ?: 0 }
        val responsesWithDuration = conversationHistory.filter { it.role == "assistant" && it.durationMs != null }
        val totalDuration = responsesWithDuration.sumOf { it.durationMs ?: 0 }
        val avgDuration = if (responsesWithDuration.isNotEmpty()) {
            totalDuration / responsesWithDuration.size
        } else 0

        return buildString {
            appendLine("Current Configuration:")
            appendLine("- Client: ${config.clientType.name.lowercase()}")
            appendLine("- Model: ${config.model}")
            appendLine("- Temperature: ${config.temperature}")
            appendLine("- System Prompt: ${config.systemPrompt.ifEmpty { "(not set)" }}")
            appendLine("- Show Tokens: ${if (config.showTokens) "enabled" else "disabled"}")
            appendLine("- Conversation History: ${conversationHistory.size} messages")
            appendLine("- Total Tokens Used: $totalTokens")
            if (responsesWithDuration.isNotEmpty()) {
                appendLine("- Average Response Time: ${String.format("%.2fs", avgDuration / 1000.0)}")
                appendLine("- Total Response Time: ${String.format("%.2fs", totalDuration / 1000.0)}")
            }
            appendLine()
        }
    }

    fun toggleShowTokens(): String {
        config.showTokens = !config.showTokens
        return "Show tokens ${if (config.showTokens) "enabled" else "disabled"}\n"
    }

    suspend fun readAndSendFile(filePath: String?): String {
        // Validate path parameter
        if (filePath.isNullOrEmpty()) {
            return "${Colors.ERROR}Please provide a file path${Colors.RESET}\n"
        }

        return try {
            val file = java.io.File(filePath)

            // Validate file existence
            if (!file.exists()) {
                return "${Colors.ERROR}File not found: $filePath${Colors.RESET}\n"
            }

            // Validate it's a file (not a directory)
            if (!file.isFile) {
                return "${Colors.ERROR}Path is not a file: $filePath${Colors.RESET}\n"
            }

            // Validate readability
            if (!file.canRead()) {
                return "${Colors.ERROR}Cannot read file (permission denied): $filePath${Colors.RESET}\n"
            }

            // Read file content
            val content = file.readText()

            // Show preview (first 200 chars)
            val preview = if (content.length > 200) {
                content.take(200) + "..."
            } else {
                content
            }

            buildString {
                appendLine("${Colors.INFO}Reading file: $filePath${Colors.RESET}")
                appendLine("${Colors.USER}File content preview:${Colors.RESET}")
                appendLine("${Colors.USER}$preview${Colors.RESET}\n")
                appendLine("${Colors.INFO}Sending to AI...${Colors.RESET}\n")

                // Send to AI and get response
                val response = sendMessage(content)
                appendLine("${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$response${Colors.RESET}\n")
            }

        } catch (e: java.io.IOException) {
            "${Colors.ERROR}Error reading file: ${e.message}${Colors.RESET}\n"
        } catch (e: Exception) {
            "${Colors.ERROR}Unexpected error: ${e.message}${Colors.RESET}\n"
        }
    }

    fun setClient(clientName: String?): String {
        if (clientName.isNullOrEmpty()) {
            return "Please provide a client name (perplexity or huggingface)\n"
        }

        val newClientType = when (clientName.lowercase()) {
            "perplexity", "pplx" -> ClientType.PERPLEXITY
            "huggingface", "hf" -> ClientType.HUGGINGFACE
            else -> {
                return "Unknown client: $clientName. Available: perplexity, huggingface\n"
            }
        }

        config.clientType = newClientType

        // Update default model based on client
        config.model = client().models().first()

        return "Client switched to ${newClientType.name.lowercase()}. Model set to ${config.model}\n"
    }

    fun listModels(): String {
        val client = client()

        return buildString {
            appendLine("Available models for ${config.clientType.name.lowercase()}:")
            client.models().forEach { model ->
                appendLine("- $model")
            }
            appendLine()
        }
    }

    private fun client(): Client = when (config.clientType) {
        ClientType.PERPLEXITY -> perplexityClient
        ClientType.HUGGINGFACE -> huggingFaceClient
    }
}