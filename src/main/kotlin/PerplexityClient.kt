package org.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*

const val API_URL = "https://api.perplexity.ai/chat/completions"

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class PerplexityRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null
)

@Serializable
data class Choice(
    val message: Message,
    val index: Int? = null,
    val finish_reason: String? = null
)

@Serializable
data class PerplexityResponse(
    val choices: List<Choice>,
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null
)

val jsonParser = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

class PerplexityClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    suspend fun sendMessage(
        conversationHistory: List<Message>,
        temperature: Double = 0.7,
        model: String = "sonar-pro",
    ): String {
        val requestInfo = StringBuilder()
        return try {
            val request = PerplexityRequest(
                model = model,
                messages = conversationHistory,
                temperature = temperature
            )

            requestInfo.append("=== REQUEST INFO ===\n")
            requestInfo.append("Model: sonar-pro\n")
            requestInfo.append("Temperature: $temperature\n")
            requestInfo.append("Messages: ${conversationHistory.size}\n")
            requestInfo.append("\nRequest JSON:\n${jsonParser.encodeToString(PerplexityRequest.serializer(), request)}\n")

            val httpResponse = client.post(API_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $API_KEY")
                setBody(request)
            }

            requestInfo.append("\n=== RESPONSE INFO ===\n")
            requestInfo.append("Status: ${httpResponse.status}\n")

            val responseBody = httpResponse.body<String>()
            requestInfo.append("\nResponse JSON:\n$responseBody\n")


            val response: PerplexityResponse = jsonParser.decodeFromString(responseBody)

            response.choices.firstOrNull()?.message?.content.orEmpty()
        } catch (e: kotlinx.serialization.SerializationException) {
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== SERIALIZATION ERROR ===\n")
                append("Error: ${e.message}\n")
                append("\nThis usually means the API returned unexpected JSON format.\n")
                append("Check the Response JSON above to see what was actually returned.\n")
            }

            errorMsg
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== HTTP CLIENT ERROR (4xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }

            errorMsg
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== HTTP SERVER ERROR (5xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }

            errorMsg
        } catch (e: Exception) {
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== GENERAL ERROR ===\n")
                append("Type: ${e.javaClass.simpleName}\n")
                append("Message: ${e.message}\n")
                append("\nStack Trace:\n${e.stackTraceToString()}\n")
            }
            errorMsg
        }
    }

    fun close() {
        client.close()
    }
}
