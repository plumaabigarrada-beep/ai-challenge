package org.example

import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.SerializationException

suspend fun errorMessage(e: Exception, requestInfo: StringBuilder, additionalNote: String? = null): String {
    return buildString {
        append(requestInfo)
        when (e) {
            is SerializationException -> {
                append("\n=== SERIALIZATION ERROR ===\n")
                append("Error: ${e.message}\n")
                append("\nThis usually means the API returned unexpected JSON format.\n")
                append("Check the Response JSON above to see what was actually returned.\n")
            }
            is ClientRequestException -> {
                val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
                append("\n=== HTTP CLIENT ERROR (4xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }
            is ServerResponseException -> {
                val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
                append("\n=== HTTP SERVER ERROR (5xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }
            else -> {
                append("\n=== GENERAL ERROR ===\n")
                append("Type: ${e.javaClass.simpleName}\n")
                append("Message: ${e.message}\n")
                append("\nStack Trace:\n${e.stackTraceToString()}\n")
            }
        }
        if (additionalNote != null) {
            append("\n$additionalNote\n")
        }
    }
}
