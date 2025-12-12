package com.jamycake.aiagent.chat

object ContextWindowConfig {
    // Default context window sizes for various models
    private val contextWindows = mapOf(
        // Perplexity models
        "llama-3.1-sonar-small-128k-online" to 127000,
        "llama-3.1-sonar-large-128k-online" to 127000,
        "llama-3.1-sonar-huge-128k-online" to 127000,
        "llama-3.1-8b-instruct" to 131072,
        "llama-3.1-70b-instruct" to 131072,

        // HuggingFace models (common ones)
        "meta-llama/Meta-Llama-3-8B-Instruct" to 8192,
        "mistralai/Mistral-7B-Instruct-v0.2" to 32768,
        "microsoft/Phi-3-mini-4k-instruct" to 4096,
        "google/gemma-7b-it" to 8192,

        // LMStudio (depends on what model is loaded, using reasonable defaults)
        "qwen2.5-coder:7b" to 131072,
        "llama-3.2" to 128000,
        "mistral" to 32768,
        "phi-3" to 4096,

        // Generic fallback
        "default" to 4096
    )

    fun getContextWindow(model: String): Int {
        // Try exact match first
        contextWindows[model]?.let { return it }

        // Try partial match (for models with version numbers or variants)
        contextWindows.entries.find { model.contains(it.key, ignoreCase = true) }?.let {
            return it.value
        }

        // Return default if no match found
        return contextWindows["default"]!!
    }

    fun getAllModels(): Map<String, Int> = contextWindows
}