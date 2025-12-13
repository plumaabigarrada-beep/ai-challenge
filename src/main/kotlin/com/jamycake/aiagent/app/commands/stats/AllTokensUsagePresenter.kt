package com.jamycake.aiagent.app.commands.stats

import com.jamycake.aiagent.domain.core.agent.TokensUsage

internal class AllTokensUsagePresenter {

    fun present(usage: List<TokensUsage>) : String {
        return usage.joinToString("\n") {
            it.present()
        }
    }

    private fun TokensUsage.present() : String {
        val usage = this
        return buildString {
            append("Prompt: ")
            append(usage.prompt_tokens)
            append(", ")
            append("Completion: ")
            append(usage.completion_tokens)
            append(", ")
            append("Total: ")
            append(usage.total_tokens)
        }
    }

}