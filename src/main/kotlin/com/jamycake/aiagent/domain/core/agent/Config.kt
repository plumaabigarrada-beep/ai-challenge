package com.jamycake.aiagent.domain.core.agent

import kotlinx.serialization.Serializable

@Serializable
enum class ClientType {
    PERPLEXITY,
    HUGGINGFACE,
    LMSTUDIO
}

@Serializable
data class Config(
    var model: String = "qwen/qwen2.5-coder-14b",
    var temperature: Double = 0.7,
    var systemPrompt: String = "",
    var clientType: ClientType = ClientType.LMSTUDIO,
    var compressionClient: ClientType = ClientType.LMSTUDIO,
    var compressionModel: String = "qwen/qwen2.5-coder-14b",
    var compressionTemperature: Double = 0.0,
    var showTokens: Boolean = true,
    var autoCompressEnabled: Boolean = false,
    var autoCompressMessagesThreshold: Int = 10,
    var autoCompressThreshold: Double = 0.80, // Compress at 80% of context window
    var autoCompressNotify: Boolean = true // Notify user when auto-compressing
)