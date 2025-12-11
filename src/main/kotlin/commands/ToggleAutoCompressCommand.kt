package commands

import org.example.Config

class ToggleAutoCompressCommand {
    fun execute(config: Config): String {
        config.autoCompressEnabled = !config.autoCompressEnabled
        return "Auto-compress ${if (config.autoCompressEnabled) "enabled" else "disabled"}\n"
    }
}
