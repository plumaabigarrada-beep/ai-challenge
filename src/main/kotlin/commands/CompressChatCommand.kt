package commands

import chat.Chat
import compressor.ChatCompressor

class CompressChatCommand {
    suspend fun execute(chatCompressor: ChatCompressor, currentChat: Chat): String {
        return try {
            val compressedChat = chatCompressor.compress(currentChat)
            "Chat history compressed successfully.\n"
        } catch (e: Exception) {
            "Error compressing chat: ${e.message}\n"
        }
    }
}
