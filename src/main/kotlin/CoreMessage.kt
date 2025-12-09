package org.example

data class CoreMessage(
    val role: String,
    val content: String,
    val tokens: Int? = null
)