package com.jamycake.aiagent.terminal.commands

import com.jamycake.aiagent.client.Client
import com.jamycake.aiagent.chat.ClientType
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.chat.Config

internal class ListModelsCommand(
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
