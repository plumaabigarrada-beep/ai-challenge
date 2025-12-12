package org.example.context

internal data class Context(
    val messages: List<ContextMessage>,
) {

    fun formatHistoryAsString(): String = buildString {
        messages.forEach { message ->
            appendLine("${message.role.uppercase()}:")
            appendLine(message.content)
            appendLine()
        }
    }

}