package org.example

import chat.Chat
import client.Client
import compressor.ChatCompressor
import chatsaver.ChatSaver
import commands.*

class App(
    private val chatCompressor: ChatCompressor,
    private val chatSaver: ChatSaver,
    private val clients: Map<ClientType, Client>,
    private val config: Config,
) {

    private val chats = mutableMapOf<String, Chat>()
    private var currentChatId: String

    // Command instances
    private val sendMessageCommand = SendMessageCommand()
    private val setTemperatureCommand = SetTemperatureCommand()
    private val setSystemPromptCommand = SetSystemPromptCommand()
    private val setModelCommand = SetModelCommand()
    private val clearHistoryCommand = ClearHistoryCommand()
    private val getConfigCommand = GetConfigCommand()
    private val toggleShowTokensCommand = ToggleShowTokensCommand()
    private val readAndSendFileCommand = ReadAndSendFileCommand()
    private val setClientCommand = SetClientCommand()
    private val listModelsCommand = ListModelsCommand()
    private val createChatCommand = CreateChatCommand()
    private val deleteChatCommand = DeleteChatCommand()
    private val switchChatCommand = SwitchChatCommand()
    private val listChatsCommand = ListChatsCommand()
    private val renameChatCommand = RenameChatCommand()
    private val compressChatCommand = CompressChatCommand()
    private val saveChatCommand = SaveChatCommand()
    private val toggleAutoCompressCommand = ToggleAutoCompressCommand()
    private val setAutoCompressThresholdCommand = SetAutoCompressThresholdCommand()

    init {
        // Create initial default chat
        val defaultChat = Chat(clients = clients, config = config, chatCompressor = chatCompressor)
        chats[defaultChat.id] = defaultChat
        currentChatId = defaultChat.id
    }


    suspend fun sendMessage(text: String): String {
        return sendMessageCommand.execute(getCurrentChat(), text)
    }


    fun close() {
        clients.values.forEach { it.close() }
    }

    fun setTemperature(temperature: Double?) : String {
        return setTemperatureCommand.execute(config, temperature)
    }

    fun setSystemPrompt(prompt: String?): String {
        return setSystemPromptCommand.execute(config, prompt)
    }

    fun setModel(model: String?): String {
        return setModelCommand.execute(config, model)
    }

    fun clearHistory(): String {
        return clearHistoryCommand.execute(getCurrentChat())
    }

    fun getConfig(): String {
        return getConfigCommand.execute(config, getCurrentChat())
    }

    fun toggleShowTokens(): String {
        return toggleShowTokensCommand.execute(config)
    }

    suspend fun readAndSendFile(filePath: String?): String {
        return readAndSendFileCommand.execute(getCurrentChat(), filePath)
    }

    fun setClient(clientName: String?): String {
        return setClientCommand.execute(config, clients, clientName)
    }

    fun listModels(): String {
        return listModelsCommand.execute(config, client())
    }

    fun createChat(name: String? = null): String {
        val (newChatId, message) = createChatCommand.execute(chats, clients, config, chatCompressor, name)
        currentChatId = newChatId
        return message
    }

    fun deleteChat(chatId: String?): String {
        val (newChatId, message) = deleteChatCommand.execute(chats, currentChatId, chatId, ::findChatByPartialId)
        if (newChatId != null) {
            currentChatId = newChatId
        }
        return message
    }

    fun switchChat(chatId: String?): String {
        val (newChatId, message) = switchChatCommand.execute(chats, chatId, ::findChatByPartialId)
        if (newChatId != null) {
            currentChatId = newChatId
        }
        return message
    }

    fun listChats(): String {
        return listChatsCommand.execute(chats, currentChatId)
    }

    fun renameChat(name: String?): String {
        return renameChatCommand.execute(getCurrentChat(), name)
    }

    suspend fun compressChat(): String {
        return compressChatCommand.execute(chatCompressor, getCurrentChat())
    }

    fun saveChat(directory: String? = null): String {
        return saveChatCommand.execute(chatSaver, getCurrentChat(), directory)
    }

    fun toggleAutoCompress(): String {
        return toggleAutoCompressCommand.execute(config)
    }

    fun setAutoCompressThreshold(threshold: Double?): String {
        return setAutoCompressThresholdCommand.execute(config, threshold)
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