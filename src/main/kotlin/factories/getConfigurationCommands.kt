package factories

import com.jamycake.aiagent.client.Client
import commands.*
import org.example.ClientType
import com.jamycake.aiagent.terminal.Command
import org.example.Config

internal fun getConfigurationCommands(
    config: Config,
    clients: Map<ClientType, Client>
): List<Command> = listOf(
    // Configuration commands
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SetClientCommand(
        config = config,
        clients = clients,
        values = listOf("--client", "-cl")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SetModelCommand(
        config = config,
        values = listOf("--model", "-m")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SetTemperatureCommand(
        config = config,
        values = listOf("--temperature", "-t")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SetSystemPromptCommand(
        config = config,
        values = listOf("--systemprompt", "-sp")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ToggleShowTokensCommand(
        config = config,
        values = listOf("--showtokens", "-st")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ToggleAutoCompressCommand(
        config = config,
        values = listOf("--autocompress", "-ac")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.SetAutoCompressThresholdCommand(
        config = config,
        values = listOf("--acthreshold", "-act")
    ),
    _root_ide_package_.com.jamycake.aiagent.terminal.commands.ListModelsCommand(
        config = config,
        clients = clients,
        values = listOf("--models", "-ls")
    ),
)