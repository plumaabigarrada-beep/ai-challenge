package commands

import client.Client
import org.example.ClientType
import org.example.Command
import org.example.Config

class ListModelsCommand(
    private val config: Config,
    private val clients: Map<ClientType, Client>,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        val client = clients[config.clientType]
            ?: return "Client not found\n"

        return buildString {
            appendLine("Available models for ${config.clientType.name.lowercase()}:")
            client.models().forEach { model ->
                appendLine("- $model")
            }
            appendLine()
        }
    }
}
