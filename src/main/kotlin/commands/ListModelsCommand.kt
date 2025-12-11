package commands

import client.Client
import org.example.Config

class ListModelsCommand {
    fun execute(config: Config, client: Client): String {
        return buildString {
            appendLine("Available models for ${config.clientType.name.lowercase()}:")
            client.models().forEach { model ->
                appendLine("- $model")
            }
            appendLine()
        }
    }
}
