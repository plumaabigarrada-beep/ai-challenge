package com.jamycake.aiagent.domain.core.agent

interface ModelTool {
    val name: String
    val description: String
    val parameters: Map<String, Any>

    suspend fun execute(arguments: Map<String, String>): ToolResult
}
