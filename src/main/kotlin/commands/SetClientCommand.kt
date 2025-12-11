package commands

import client.Client
import org.example.ClientType
import org.example.Command
import org.example.Config

class SetClientCommand(
    private val config: Config,
    private val clients: Map<ClientType, Client>,
    values: List<String>
) : Command(values) {
    override suspend fun execute(args: String?): String {
        if (args.isNullOrEmpty()) {
            return "Please provide a client name (perplexity, huggingface, or lmstudio)\n"
        }

        val newClientType = when (args.lowercase()) {
            "perplexity", "pplx" -> ClientType.PERPLEXITY
            "huggingface", "hf" -> ClientType.HUGGINGFACE
            "lmstudio", "lm" -> ClientType.LMSTUDIO
            else -> {
                return "Unknown client: $args. Available: perplexity, huggingface, lmstudio\n"
            }
        }

        config.clientType = newClientType

        // Update default model based on client
        val client = clients[newClientType] ?: throw IllegalStateException("Client not registered")
        config.model = client.models().first()

        return "Client switched to ${newClientType.name.lowercase()}. Model set to ${config.model}\n"
    }
}
