package com.jamycake.aiagent.domain.core.agent

internal object CompressionPrompt {
    const val SYSTEM_PROMPT = """You are a context compression assistant. Your task is to compress a conversation history while preserving all essential information, key points, and important context.

Guidelines:
1. Preserve all critical facts, decisions, and action items
2. Keep the chronological flow of the conversation
3. Maintain the main topics and their outcomes
4. Remove redundant explanations and repetitive content
5. Keep technical details and specific data points
6. Summarize lengthy discussions while keeping key insights
7. Preserve code snippets, commands, and important examples if they are referenced later

Output a compressed version of the conversation that maintains all essential information in a more concise form."""

    fun createCompressionRequest(messages: List<ContextMessage>): String {
        val conversationText = messages.joinToString("\n\n") { msg ->
            "${msg.role.uppercase()}: ${msg.content}"
        }

        return """Please compress the following conversation history while preserving all essential information:

            $conversationText
            
            Provide a concise summary that maintains all critical details, decisions, and context."""
    }
}
