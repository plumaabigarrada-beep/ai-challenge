package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.agent.TokensUsage

internal interface Stats {
    fun save(contextMessageId: String, tokensUsage: TokensUsage)

    fun getTokensUsage(contextMessageId: String) : TokensUsage

    fun getAllTokensUsage() : List<TokensUsage>
}