package org.example

import chat.Chat
import client.Client
import compressor.ChatCompressor

class App(
    private val chatCompressor: ChatCompressor,
    private val clients: Map<ClientType, Client>,
    private val config: Config,
) {

    private val chats = mutableMapOf<String, Chat>()
    private var currentChatId: String

    init {
        // Create initial default chat
        val defaultChat = Chat(clients = clients, config = config)
        chats[defaultChat.id] = defaultChat
        currentChatId = defaultChat.id
    }


    suspend fun sendMessage(text: String): String {
        return getCurrentChat().sendMessage(text)
    }


    fun close() {
        clients.values.forEach { it.close() }
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
        return getCurrentChat().clearHistory()
    }

    fun getConfig(): String {
        val currentChat = getCurrentChat()
        val stats = currentChat.getStats()

        return buildString {
            appendLine("Current Configuration:")
            appendLine("- Client: ${config.clientType.name.lowercase()}")
            appendLine("- Model: ${config.model}")
            appendLine("- Temperature: ${config.temperature}")
            appendLine("- System Prompt: ${config.systemPrompt.ifEmpty { "(not set)" }}")
            appendLine("- Show Tokens: ${if (config.showTokens) "enabled" else "disabled"}")
            appendLine("- Current Chat: ${currentChat.name}")
            appendLine("- Conversation History: ${stats.messageCount} messages")
            appendLine("- Total Tokens Used: ${stats.totalTokens}")
            if (stats.avgResponseTime > 0) {
                appendLine("- Average Response Time: ${String.format("%.2fs", stats.avgResponseTime / 1000.0)}")
                appendLine("- Total Response Time: ${String.format("%.2fs", stats.totalResponseTime / 1000.0)}")
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
            return "Please provide a client name (perplexity, huggingface, or lmstudio)\n"
        }

        val newClientType = when (clientName.lowercase()) {
            "perplexity", "pplx" -> ClientType.PERPLEXITY
            "huggingface", "hf" -> ClientType.HUGGINGFACE
            "lmstudio", "lm" -> ClientType.LMSTUDIO
            else -> {
                return "Unknown client: $clientName. Available: perplexity, huggingface, lmstudio\n"
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

    fun createChat(name: String? = null): String {
        val newChat = Chat(
            name = name ?: "Chat ${chats.size + 1}",
            clients = clients,
            config = config
        )
        chats[newChat.id] = newChat
        currentChatId = newChat.id
        return "Created and switched to new chat: ${newChat.name}\n"
    }

    fun deleteChat(chatId: String?): String {
        if (chatId.isNullOrEmpty()) {
            return "Please provide a chat ID to delete\n"
        }

        if (chats.size == 1) {
            return "Cannot delete the last chat. Create a new one first.\n"
        }

        // Support partial ID matching
        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        if (fullChatId == currentChatId) {
            // Switch to another chat before deleting
            val anotherChatId = chats.keys.first { it != fullChatId }
            currentChatId = anotherChatId
        }

        val deletedChat = chats.remove(fullChatId)
        return "Deleted chat: ${deletedChat?.name}. Switched to: ${getCurrentChat().name}\n"
    }

    fun switchChat(chatId: String?): String {
        if (chatId.isNullOrEmpty()) {
            return "Please provide a chat ID\n"
        }

        // Support partial ID matching
        val fullChatId = findChatByPartialId(chatId)
        if (fullChatId == null) {
            return "Chat not found: $chatId\n"
        }

        currentChatId = fullChatId
        val currentChat = getCurrentChat()
        return "Switched to chat: ${currentChat.name}\n"
    }

    fun listChats(): String {
        return buildString {
            appendLine("Available chats:")
            chats.forEach { (id, chat) ->
                val stats = chat.getStats()
                val current = if (id == currentChatId) " (current)" else ""
                appendLine("- [${id.take(8)}] ${chat.name}$current - ${stats.messageCount} messages, ${stats.totalTokens} tokens")
            }
            appendLine()
        }
    }

    fun renameChat(name: String?): String {
        if (name.isNullOrEmpty()) {
            return "Please provide a new name for the chat\n"
        }
        getCurrentChat().name = name
        return "Chat renamed to: $name\n"
    }

    private fun getCurrentChat(): Chat {
        return chats[currentChatId] ?: throw IllegalStateException("Current chat not found")
    }

    private fun findChatByPartialId(partialId: String): String? {
        // First try exact match
        if (chats.containsKey(partialId)) {
            return partialId
        }

        // Then try partial match (case-insensitive)
        val matches = chats.keys.filter { it.startsWith(partialId, ignoreCase = true) }

        return when {
            matches.isEmpty() -> null
            matches.size == 1 -> matches.first()
            else -> {
                // Multiple matches - return exact prefix match if available
                matches.firstOrNull { it.startsWith(partialId, ignoreCase = false) }
                    ?: matches.first() // Otherwise return first match
            }
        }
    }

    private fun client(): Client = clients[config.clientType] ?: throw IllegalStateException("Client not registered")
}