package commands

import org.example.Command
import org.example.Config

class SetAutoCompressThresholdCommand(
    private val config: Config,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val threshold = args?.toDoubleOrNull()

        if (threshold == null) {
            return "Current auto-compress threshold: ${(config.autoCompressThreshold * 100).toInt()}%\n"
        }

        if (threshold !in 0.5..0.95) {
            return "Invalid threshold. Please provide a value between 0.5 (50%) and 0.95 (95%)\n"
        }

        config.autoCompressThreshold = threshold
        return "Auto-compress threshold set to ${(threshold * 100).toInt()}%\n"
    }
}
