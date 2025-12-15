package com.jamycake.aiagent.domain.core.tools

import com.jamycake.aiagent.domain.core.agent.ModelTool
import com.jamycake.aiagent.domain.core.agent.ToolResult
import java.io.File

class FilesStructureTool : ModelTool {
    override val name = "files_structure"

    override val description = "Shows the complete file and folder structure of a specified directory path with hierarchical leveling"

    override val parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "path" to mapOf(
                "type" to "string",
                "description" to "The absolute or relative path to the directory to analyze"
            ),
            "maxDepth" to mapOf(
                "type" to "string",
                "description" to "Maximum depth to traverse (optional, default is unlimited)"
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

        val maxDepth = arguments["maxDepth"]?.toIntOrNull() ?: Int.MAX_VALUE

        val directory = File(path)
        if (!directory.exists()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Directory does not exist: $path",
                isSuccess = false
            )
        }

        if (!directory.isDirectory) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Path is not a directory: $path",
                isSuccess = false
            )
        }

        val output = buildString {
            appendLine(directory.name)
            listStructure(directory, "", true, 0, maxDepth, this)
        }

        return ToolResult(
            callId = "",
            output = output,
            isSuccess = true
        )
    }

    private fun listStructure(
        directory: File,
        prefix: String,
        isLast: Boolean,
        currentDepth: Int,
        maxDepth: Int,
        builder: StringBuilder
    ) {
        if (currentDepth >= maxDepth) return

        val items = directory.listFiles()?.sortedWith(
            compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: return

        items.forEachIndexed { index, file ->
            val isLastItem = index == items.size - 1
            val connector = if (isLastItem) "└── " else "├── "
            val newPrefix = if (isLastItem) "    " else "│   "

            builder.append(prefix)
            builder.append(connector)
            builder.appendLine(file.name)

            if (file.isDirectory) {
                listStructure(
                    file,
                    prefix + newPrefix,
                    isLastItem,
                    currentDepth + 1,
                    maxDepth,
                    builder
                )
            }
        }
    }
}
