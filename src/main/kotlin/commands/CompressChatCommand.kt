package commands

import chatcontainer.ChatContainer
import compressor.ChatCompressor
import org.example.Command

class CompressChatCommand(
    private val chatContainer: ChatContainer,
    private val chatCompressor: ChatCompressor,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return try {
            chatCompressor.compress(currentChat)
            "Chat history compressed successfully.\n"
        } catch (e: Exception) {
            "Error compressing chat: ${e.message}\n"
        }
    }
}
