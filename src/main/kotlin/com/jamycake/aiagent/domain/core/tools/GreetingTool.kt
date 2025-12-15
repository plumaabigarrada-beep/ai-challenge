package com.jamycake.aiagent.domain.core.tools

import com.jamycake.aiagent.domain.core.agent.ModelTool
import com.jamycake.aiagent.domain.core.agent.ToolResult

class GreetingTool : ModelTool {
    override val name = "greeting"

    override val description = "A simple tool that returns a friendly greeting message"

    override val parameters = mapOf(
        "type" to "object",
        "properties" to emptyMap<String, Any>(),
        "required" to emptyList<String>()
    )

    override suspend fun execute(arguments: Map<String, String>): ToolResult {
        return ToolResult(
            callId = "",
            output = "Hello, AI! I am your tool result.",
            isSuccess = true
        )
    }
}
