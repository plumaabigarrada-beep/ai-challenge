package com.jamycake.aiagent.domain.core.tools

import com.jamycake.aiagent.domain.core.agent.ModelTool
import com.jamycake.aiagent.domain.core.agent.ToolCall
import com.jamycake.aiagent.domain.core.agent.ToolResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class Tools {
    private val tools = mutableMapOf<String, ModelTool>()

    init {
        registerTool(GreetingTool())
        registerTool(FilesStructureTool())
        registerTool(ReadFileTool())
        registerTool(FindFilePathTool())
    }

    fun registerTool(tool: ModelTool) {
        tools[tool.name] = tool
    }

    fun removeTool(toolName: String) {
        tools.remove(toolName)
    }

    fun getTool(toolName: String): ModelTool? {
        return tools[toolName]
    }

    fun getAllTools(): List<ModelTool> {
        return tools.values.toList()
    }

    suspend fun executeToolCalls(calls: List<ToolCall>): List<ToolResult> {
        return coroutineScope {
            calls.map { call ->
                async {
                    try {
                        val result = tools[call.name]?.execute(call.arguments)
                            ?: ToolResult(
                                callId = call.id,
                                output = "",
                                error = "Tool '${call.name}' not found",
                                isSuccess = false
                            )
                        result.copy(callId = call.id)
                    } catch (e: Exception) {
                        ToolResult(
                            callId = call.id,
                            output = "",
                            error = e.message ?: "Unknown error",
                            isSuccess = false
                        )
                    }
                }
            }.awaitAll()
        }
    }
}
