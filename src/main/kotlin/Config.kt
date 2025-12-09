package org.example

enum class ClientType {
    PERPLEXITY,
    HUGGINGFACE
}

data class Config(
    var model: String = "sonar-pro",
    var temperature: Double = 0.7,
    var systemPrompt: String = "",
    var clientType: ClientType = ClientType.PERPLEXITY,
    var showTokens: Boolean = true
)