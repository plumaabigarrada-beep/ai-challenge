package com.jamycake.aiagent.domain.core.tools

import com.jamycake.aiagent.domain.core.agent.ModelTool
import com.jamycake.aiagent.domain.core.agent.ToolResult
import java.io.File

class FindFilePathTool : ModelTool {
    override val name = "find_file_path"

    override val description = "Searches for files by name or pattern in a directory and returns their absolute paths"

    override val parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "directory" to mapOf(
                "type" to "string",
                "description" to "The directory to search in (absolute or relative path)"
            ),
            "fileName" to mapOf(
                "type" to "string",
                "description" to "The file name or pattern to search for (supports wildcards like *.kt)"
            ),
            "maxDepth" to mapOf(
                "type" to "string",
                "description" to "Maximum depth to search (optional, default is unlimited)"
            )
        ),
        "required" to listOf("directory", "fileName")
    )

    override suspend fun execute(arguments: Map<String, String>): ToolResult {
        val directory = arguments["directory"]
        val fileName = arguments["fileName"]

        if (directory.isNullOrEmpty()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Directory argument is required",
                isSuccess = false
            )
        }

        if (fileName.isNullOrEmpty()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "fileName argument is required",
                isSuccess = false
            )
        }

        val maxDepth = arguments["maxDepth"]?.toIntOrNull() ?: Int.MAX_VALUE

        val dir = File(directory)
        if (!dir.exists()) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Directory does not exist: $directory",
                isSuccess = false
            )
        }

        if (!dir.isDirectory) {
            return ToolResult(
                callId = "",
                output = "",
                error = "Path is not a directory: $directory",
                isSuccess = false
            )
        }

        val results = mutableListOf<String>()
        searchFiles(dir, fileName, 0, maxDepth, results)

        val output = if (results.isEmpty()) {
            "No files found matching '$fileName' in $directory"
        } else {
            buildString {
                appendLine("Found ${results.size} file(s):")
                results.forEach { path ->
                    appendLine(path)
                }
            }
        }

        return ToolResult(
            callId = "",
            output = output,
            isSuccess = true
        )
    }

    private fun searchFiles(
        directory: File,
        pattern: String,
        currentDepth: Int,
        maxDepth: Int,
        results: MutableList<String>
    ) {
        if (currentDepth >= maxDepth) return

        val files = directory.listFiles() ?: return

        files.forEach { file ->
            if (file.isFile && matchesPattern(file.name, pattern)) {
                results.add(file.absolutePath)
            } else if (file.isDirectory) {
                searchFiles(file, pattern, currentDepth + 1, maxDepth, results)
            }
        }
    }

    private fun matchesPattern(fileName: String, pattern: String): Boolean {
        // Convert wildcard pattern to regex
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex(RegexOption.IGNORE_CASE)

        return regex.matches(fileName)
    }
}
