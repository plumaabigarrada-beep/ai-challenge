package org.example


abstract class Command(
    val values: List<String>
) {
    fun matches(text: String): Boolean {
        val command = text.trim().substringBefore(" ")
        return values.any { value ->
            command.equals(value, ignoreCase = true)
        }
    }

    fun extractValue(input: String): String? {
        for (value in values) {
            if (input.lowercase().startsWith(value.lowercase())) {
                return input.substring(value.length).trim()
            }
        }
        return null
    }

    fun extractDoubleValue(input: String) : Double? {
        return extractValue(input)?.toDoubleOrNull()
    }

    abstract suspend fun execute(args: String?): String
}