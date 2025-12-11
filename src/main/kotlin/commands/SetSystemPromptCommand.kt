package commands

import org.example.Config

class SetSystemPromptCommand {
    fun execute(config: Config, prompt: String?): String {
        if (prompt.isNullOrEmpty()) {
            return "Please provide a system prompt\n"
        }

        config.systemPrompt = prompt

        return "System prompt set.\n"
    }
}
