package factories

import client.Client
import commands.*
import org.example.ClientType
import org.example.Command
import org.example.Config

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