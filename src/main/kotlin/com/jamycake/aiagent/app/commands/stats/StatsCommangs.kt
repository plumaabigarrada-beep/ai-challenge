package com.jamycake.aiagent.app.commands.stats

import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.terminal.Command
import com.jamycake.aiagent.terminal.TerminalUI

internal fun statsCommands(
    stats: Stats,
    terminalUI: TerminalUI
) : List<Command> {
    return listOf(
        StatsCommand(stats = stats, terminalUI = terminalUI)
    )
}