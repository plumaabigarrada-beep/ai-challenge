package commands

import chatcontainer.ChatContainer
import org.example.Colors
import org.example.Command

internal class ReadAndSendFileCommand(
    private val chatContainer: ChatContainer,
    private val sendMessageCommand: SendMessageCommand,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        // Validate path parameter
        if (args.isNullOrEmpty()) {
            return "${Colors.ERROR}Please provide a file path${Colors.RESET}\n"
        }

        val currentChat = chatContainer.getCurrentChat()
            ?: return "No active chat\n"

        return try {
            val file = java.io.File(args)

            // Validate file existence
            if (!file.exists()) {
                return "${Colors.ERROR}File not found: $args${Colors.RESET}\n"
            }

            // Validate it's a file (not a directory)
            if (!file.isFile) {
                return "${Colors.ERROR}Path is not a file: $args${Colors.RESET}\n"
            }

            // Validate readability
            if (!file.canRead()) {
                return "${Colors.ERROR}Cannot read file (permission denied): $args${Colors.RESET}\n"
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
                appendLine("${Colors.INFO}Reading file: $args${Colors.RESET}")
                appendLine("${Colors.USER}File content preview:${Colors.RESET}")
                appendLine("${Colors.USER}$preview${Colors.RESET}\n")
                appendLine("${Colors.INFO}Sending to AI...${Colors.RESET}\n")

                // Send to AI and get response via SendMessageCommand
                val response = sendMessageCommand.execute(content)
                appendLine("${Colors.ASSISTANT}${Colors.BOLD}Assistant:${Colors.RESET} ${Colors.ASSISTANT}$response${Colors.RESET}\n")
            }

        } catch (e: java.io.IOException) {
            "${Colors.ERROR}Error reading file: ${e.message}${Colors.RESET}\n"
        } catch (e: Exception) {
            "${Colors.ERROR}Unexpected error: ${e.message}${Colors.RESET}\n"
        }
    }
}
