package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.core.tools.Tools

internal class Context(
    messages: List<ContextMessage>,
    val tools: Tools,
    val protocol: Protocol = Protocol()
) {

    private val _messages = messages.toMutableList()
    val messages: List<ContextMessage> get() = _messages.toList()


    fun addMessage(message: ContextMessage) {
        _messages.add(message)
    }

    fun addToolResults(results: List<ToolResult>) {
        results.forEach { result ->
            _messages.add(
                ContextMessage(
                    role = "tool",
                    content = if (result.isSuccess) result.output else "Error: ${result.error}",
                    toolCallId = result.callId
                )
            )
        }
    }

    fun clearMessages(){
        _messages.clear()
    }


}