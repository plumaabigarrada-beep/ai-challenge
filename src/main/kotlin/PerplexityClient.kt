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
data class PerplexityMessage(
    val role: String,
    val content: String
)

@Serializable
data class PerplexityRequest(
    val model: String,
    val messages: List<PerplexityMessage>,
    val temperature: Double? = null
)

@Serializable
data class Choice(
    val message: PerplexityMessage,
    val index: Int? = null,
    val finish_reason: String? = null
)

@Serializable
data class Usage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)

@Serializable
data class PerplexityResponse(
    val choices: List<Choice>,
    val usage: Usage? = null,
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null
)

val jsonParser = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

class PerplexityClient : Client{
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    override suspend fun sendMessage(
        conversationHistory: List<CoreMessage>,
        temperature: Double,
        model: String,
    ): CoreClientResponse {
        val requestInfo = StringBuilder()
        return try {
            // Convert CoreMessage to PerplexityMessage
            val messages = conversationHistory.map {
                PerplexityMessage(role = it.role, content = it.content)
            }

            val request = PerplexityRequest(
                model = model,
                messages = messages,
                temperature = temperature
            )

            requestInfo.append("=== REQUEST INFO ===\n")
            requestInfo.append("Model: $model\n")
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

            CoreClientResponse(
                content = response.choices.firstOrNull()?.message?.content.orEmpty(),
                promptTokens = response.usage?.prompt_tokens,
                responseTokens = response.usage?.completion_tokens
            )
        } catch (e: kotlinx.serialization.SerializationException) {
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== SERIALIZATION ERROR ===\n")
                append("Error: ${e.message}\n")
                append("\nThis usually means the API returned unexpected JSON format.\n")
                append("Check the Response JSON above to see what was actually returned.\n")
            }
            CoreClientResponse(content = errorMsg)
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== HTTP CLIENT ERROR (4xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }
            CoreClientResponse(content = errorMsg)
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            val responseBody = try { e.response.body<String>() } catch (ex: Exception) { "Unable to read response" }
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== HTTP SERVER ERROR (5xx) ===\n")
                append("Status: ${e.response.status}\n")
                append("Message: ${e.message}\n")
                append("\nResponse Body:\n$responseBody\n")
            }
            CoreClientResponse(content = errorMsg)
        } catch (e: Exception) {
            val errorMsg = buildString {
                append(requestInfo)
                append("\n=== GENERAL ERROR ===\n")
                append("Type: ${e.javaClass.simpleName}\n")
                append("Message: ${e.message}\n")
                append("\nStack Trace:\n${e.stackTraceToString()}\n")
            }
            CoreClientResponse(content = errorMsg)
        }
    }

    override fun models(): List<String> {
        return listOf(
            "sonar-pro",
            "sonar",
            "sonar-reasoning"
        )
    }

    fun close() {
        client.close()
    }
}
