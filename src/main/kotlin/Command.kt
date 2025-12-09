package org.example

data class Command(
    val values: List<String>
) {
    fun matches(text: String): Boolean {
        return values.any { value ->
            text.lowercase().startsWith(value.lowercase())
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
}