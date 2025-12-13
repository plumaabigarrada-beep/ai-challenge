package com.jamycake.aiagent.app.commands.stats

import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal fun statsCommands(
    stats: Stats,
    terminalUI: UI
) : List<Command> {
    return listOf(
        StatsCommand(stats = stats, terminalUI = terminalUI)
    )
}