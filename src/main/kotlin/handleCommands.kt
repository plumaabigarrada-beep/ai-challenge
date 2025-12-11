import org.example.App
import org.example.Colors
import org.example.Commands
import org.example.HELP_TEXT

suspend fun handleCommands(userInput: String, app: App): String = when {

    Commands.help.matches(userInput) -> {
        HELP_TEXT
    }

    Commands.temperature.matches(userInput) -> {
        val temp = Commands.temperature.extractDoubleValue(userInput)
        app.setTemperature(temp)
    }

    Commands.model.matches(userInput) -> {
        val modelName = Commands.model.extractValue(userInput)
        app.setModel(modelName)
    }

    Commands.systemPrompt.matches(userInput) -> {
        val prompt = Commands.systemPrompt.extractValue(userInput)
        app.setSystemPrompt(prompt)
    }

    Commands.clear.matches(userInput) -> {
        app.clearHistory()
    }

    Commands.config.matches(userInput) -> {
        app.getConfig()
    }

    Commands.client.matches(userInput) -> {
        val clientName = Commands.client.extractValue(userInput)
        app.setClient(clientName)
    }

    Commands.models.matches(userInput) -> {
        app.listModels()
    }

    Commands.showTokens.matches(userInput) -> {
        app.toggleShowTokens()
    }

    Commands.file.matches(userInput) -> {
        val filePath = Commands.file.extractValue(userInput)
        app.readAndSendFile(filePath)
    }

    Commands.createChat.matches(userInput) -> {
        val name = Commands.createChat.extractValue(userInput)
        app.createChat(name)
    }

    Commands.deleteChat.matches(userInput) -> {
        val chatId = Commands.deleteChat.extractValue(userInput)
        app.deleteChat(chatId)
    }

    Commands.switchChat.matches(userInput) -> {
        val chatId = Commands.switchChat.extractValue(userInput)
        app.switchChat(chatId)
    }

    Commands.listChats.matches(userInput) -> {
        app.listChats()
    }

    Commands.renameChat.matches(userInput) -> {
        val name = Commands.renameChat.extractValue(userInput)
        app.renameChat(name)
    }

    Commands.compress.matches(userInput) -> {
        app.compressChat()
    }

    Commands.save.matches(userInput) -> {
        val directory = Commands.save.extractValue(userInput)
        app.saveChat(directory)
    }

    Commands.autoCompress.matches(userInput) -> {
        app.toggleAutoCompress()
    }

    Commands.autoCompressThreshold.matches(userInput) -> {
        val threshold = Commands.autoCompressThreshold.extractDoubleValue(userInput)
        app.setAutoCompressThreshold(threshold)
    }

    else -> {
        val response = app.sendMessage(userInput)
        "\n${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$response${Colors.RESET}\n"
    }
}