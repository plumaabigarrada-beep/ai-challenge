package com.jamycake.aiagent.domain.core.tools

import com.jamycake.aiagent.domain.core.agent.ModelTool
import com.jamycake.aiagent.domain.core.agent.ToolResult
import java.io.File

class ReadFileTool : ModelTool {
    override val name = "read_file"

    override val description = "Reads the content of a file from the specified path"

    override val parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "path" to mapOf(
                "type" to "string",
                "description" to "The absolute or relative path to the file to read"
            )
        ),
        "required" to listOf("path")
    )

    override suspend fun execute(arguments: Map<String, String>): ToolResult {
        val path = arguments["path"]
        if (path.isNullOrEmpty()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Path argument is required",
                isSuccess = false
            )
        }

        val file = File(path)
        if (!file.exists()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "File does not exist: $path",
                isSuccess = false
            )
        }

        if (!file.isFile) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Path is not a file: $path",
                isSuccess = false
            )
        }

        return try {
            val content = file.readText()
            ToolResult(
                callId = "",
                output = content,
                isSuccess = true
            )
        } catch (e: Exception) {
            ToolResult(
                callId = "",
                output = "",
                error = "Failed to read file: ${e.message}",
                isSuccess = false
            )
        }
    }
}
