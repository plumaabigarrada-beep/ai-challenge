package com.jamycake.aiagent.domain.core.agent

class Protocol {
    private val enabledTools = mutableMapOf<String, ModelTool>()

    fun registerTool(tool: ModelTool) {
        enabledTools[tool.name] = tool
    }

    fun removeTool(toolName: String) {
        enabledTools.remove(toolName)
    }

    fun getTools(): List<ModelTool> = enabledTools.values.toList()

    fun getToolDefinitions(): List<Map<String, Any>> {
        return enabledTools.values.map { tool ->
            mapOf(
                "type" to "function",
                "function" to mapOf(
                    "name" to tool.name,
                    "description" to tool.description,
                    "parameters" to tool.parameters
                )
            )
        }
    }
}
