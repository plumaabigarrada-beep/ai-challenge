package commands

import org.example.Config

class ToggleShowTokensCommand {
    fun execute(config: Config): String {
        config.showTokens = !config.showTokens
        return "Show tokens ${if (config.showTokens) "enabled" else "disabled"}\n"
    }
}
