package commands

import client.Client
import org.example.ClientType
import org.example.Config

class SetClientCommand {
    fun execute(config: Config, clients: Map<ClientType, Client>, clientName: String?): String {
        if (clientName.isNullOrEmpty()) {
            return "Please provide a client name (perplexity, huggingface, or lmstudio)\n"
        }

        val newClientType = when (clientName.lowercase()) {
            "perplexity", "pplx" -> ClientType.PERPLEXITY
            "huggingface", "hf" -> ClientType.HUGGINGFACE
            "lmstudio", "lm" -> ClientType.LMSTUDIO
            else -> {
                return "Unknown client: $clientName. Available: perplexity, huggingface, lmstudio\n"
            }
        }

        config.clientType = newClientType

        // Update default model based on client
        val client = clients[newClientType] ?: throw IllegalStateException("Client not registered")
        config.model = client.models().first()

        return "Client switched to ${newClientType.name.lowercase()}. Model set to ${config.model}\n"
    }
}
