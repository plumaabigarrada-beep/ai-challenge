package com.jamycake.aiagent.domain.core.agent

internal data class TokensUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)