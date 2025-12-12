package com.jamycake.aiagent.client

data class CoreClientResponse(
    val content: String,
    val promptTokens: Int? = null,
    val responseTokens: Int? = null
)