package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.agent.TokensUsage
import com.jamycake.aiagent.domain.slots.Stats

internal class RamStats : Stats {

    private val stats = mutableMapOf<String, TokensUsage>()

    override fun save(
        contextMessageId: String,
        tokensUsage: TokensUsage
    ) {
        stats[contextMessageId] = tokensUsage
    }

    override fun getTokensUsage(contextMessageId: String): TokensUsage {
        return stats[contextMessageId]!!
    }

    override fun getAllTokensUsage(): List<TokensUsage> {
        return stats.values.toList()
    }
}