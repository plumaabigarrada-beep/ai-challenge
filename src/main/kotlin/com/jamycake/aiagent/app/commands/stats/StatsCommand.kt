package com.jamycake.aiagent.app.commands.stats

import com.jamycake.aiagent.domain.slots.Stats
import com.jamycake.aiagent.domain.slots.UI
import com.jamycake.aiagent.terminal.Command

internal class StatsCommand(
    private val stats: Stats,
    private val terminalUI: UI
) : Command(listOf("--stats")) {

    private val allTokensUsagePresenter = AllTokensUsagePresenter()

    override suspend fun execute(args: String?) {
        val allTokens = stats.getAllTokensUsage()
        val message = allTokensUsagePresenter.present(allTokens)
        terminalUI.out(message)
    }
}