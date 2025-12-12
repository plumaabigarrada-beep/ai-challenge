package com.jamycake.aiagent.factories

import com.jamycake.aiagent.client.Client
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.commands.*
import com.jamycake.aiagent.chat.ClientType
import com.jamycake.aiagent.chat.Config

internal fun getConfigurationCommands(
    config: Config,
    clients: Map<ClientType, Client>
): List<Command> = listOf(
    // Configuration commands
    SetClientCommand(
        config = config,
        clients = clients,
        values = listOf("--client", "-cl")
    ),
    SetModelCommand(
        config = config,
        values = listOf("--model", "-m")
    ),
    SetTemperatureCommand(
        config = config,
        values = listOf("--temperature", "-t")
    ),
    SetSystemPromptCommand(
        config = config,
        values = listOf("--systemprompt", "-sp")
    ),
    ToggleShowTokensCommand(
        config = config,
        values = listOf("--showtokens", "-st")
    ),
    ToggleAutoCompressCommand(
        config = config,
        values = listOf("--autocompress", "-ac")
    ),
    SetAutoCompressThresholdCommand(
        config = config,
        values = listOf("--acthreshold", "-act")
    ),
    ListModelsCommand(
        config = config,
        clients = clients,
        values = listOf("--models", "-ls")
    ),
)