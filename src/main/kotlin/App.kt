package org.example

class App {

    private val config = Config()
    private val client = PerplexityClient()
    private val conversationHistory = mutableListOf<Message>()


    suspend fun sendMessage(text: String) : String {

        conversationHistory.add(Message(role = "user", content = text))

        val history = if (config.systemPrompt.isEmpty()) {
            conversationHistory
        } else {
            listOf(Message(role = "system", content = config.systemPrompt)) + conversationHistory
        }

        val response = client.sendMessage(
            conversationHistory = history,
            temperature = config.temperature,
            model = config.model
        )

        if (response.isNotEmpty()) {
            conversationHistory.add(Message(role = "assistant", content = response))
        } else {
            conversationHistory.removeLastOrNull()
        }

        return response
    }


    fun exit() {
        client.close()
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
            conversationHistory.add(Message(role = "system", content = config.systemPrompt))
        }
        return "Conversation history cleared.\n"
    }

    fun getConfig(): String {
        return buildString {
            appendLine("Current Configuration:")
            appendLine("- Model: ${config.model}")
            appendLine("- Temperature: ${config.temperature}")
            appendLine("- System Prompt: ${config.systemPrompt.ifEmpty { "(not set)" }}")
            appendLine("- Conversation History: ${conversationHistory.size} messages")
            appendLine()
        }
    }
}