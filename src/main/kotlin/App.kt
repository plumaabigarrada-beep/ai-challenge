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

        val client = when(config.clientType) {
            ClientType.PERPLEXITY -> perplexityClient
            ClientType.HUGGINGFACE -> huggingFaceClient
        }

        val response = client.sendMessage(
            conversationHistory = history,
            temperature = config.temperature,
            model = config.model
        )

        if (response.content.isNotEmpty()) {
            // Update the last user message with prompt tokens
            if (response.promptTokens != null && conversationHistory.isNotEmpty()) {
                val lastIndex = conversationHistory.lastIndex
                conversationHistory[lastIndex] = conversationHistory[lastIndex].copy(
                    tokens = response.promptTokens
                )
            }

            // Add assistant response with response tokens
            conversationHistory.add(
                CoreMessage(
                    role = "assistant",
                    content = response.content,
                    tokens = response.responseTokens
                )
            )

            // Build response with optional token information
            return if (config.showTokens && (response.promptTokens != null || response.responseTokens != null)) {
                buildString {
                    append(response.content)
                    append("\n\n[Tokens: ")
                    if (response.promptTokens != null) append("prompt=${ response.promptTokens}")
                    if (response.promptTokens != null && response.responseTokens != null) append(", ")
                    if (response.responseTokens != null) append("response=${response.responseTokens}")
                    val total = (response.promptTokens ?: 0) + (response.responseTokens ?: 0)
                    append(", total=$total]")
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
        return buildString {
            appendLine("Current Configuration:")
            appendLine("- Client: ${config.clientType.name.lowercase()}")
            appendLine("- Model: ${config.model}")
            appendLine("- Temperature: ${config.temperature}")
            appendLine("- System Prompt: ${config.systemPrompt.ifEmpty { "(not set)" }}")
            appendLine("- Show Tokens: ${if (config.showTokens) "enabled" else "disabled"}")
            appendLine("- Conversation History: ${conversationHistory.size} messages")
            appendLine("- Total Tokens Used: $totalTokens")
            appendLine()
        }
    }

    fun toggleShowTokens(): String {
        config.showTokens = !config.showTokens
        return "Show tokens ${if (config.showTokens) "enabled" else "disabled"}\n"
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
        config.model = when (newClientType) {
            ClientType.PERPLEXITY -> "sonar-pro"
            ClientType.HUGGINGFACE -> "meta-llama/Llama-3.1-8B-Instruct:nscale"
        }

        return "Client switched to ${newClientType.name.lowercase()}. Model set to ${config.model}\n"
    }

    fun listModels(): String {
        val client = when (config.clientType) {
            ClientType.PERPLEXITY -> perplexityClient
            ClientType.HUGGINGFACE -> huggingFaceClient
        }

        return buildString {
            appendLine("Available models for ${config.clientType.name.lowercase()}:")
            client.models().forEach { model ->
                appendLine("- $model")
            }
            appendLine()
        }
    }
}