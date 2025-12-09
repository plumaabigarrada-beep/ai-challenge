package org.example

data class CoreClientResponse(
    val content: String,
    val promptTokens: Int? = null,
    val responseTokens: Int? = null
)