package commands

import org.example.Command
import org.example.HELP_TEXT

class HelpCommand(
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        return HELP_TEXT
    }
}
