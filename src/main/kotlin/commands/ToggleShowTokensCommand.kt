package commands

import org.example.Command
import org.example.Config

class ToggleShowTokensCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        config.showTokens = !config.showTokens
        return "Show tokens ${if (config.showTokens) "enabled" else "disabled"}\n"
    }
}
