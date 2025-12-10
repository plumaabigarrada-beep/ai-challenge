package org.example

import client.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

const val LMSTUDIO_API_URL = "http://localhost:1234/v1/chat/completions"

class LMStudioClient : Client {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 180_000 // 3 minutes
            connectTimeoutMillis = 180_000
            socketTimeoutMillis = 180_000
        }
    }

    override suspend fun sendMessage(
        conversationHistory: List<CoreMessage>,
        temperature: Double,
        model: String,
    ): CoreClientResponse {
        val requestInfo = StringBuilder()
        return try {
            // Convert CoreMessage to ChatMessage
            val messages = conversationHistory.map {
                ChatMessage(role = it.role, content = it.content)
            }

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = temperature
            )

            requestInfo.append("=== REQUEST INFO ===\n")
            requestInfo.append("Model: $model\n")
            requestInfo.append("Temperature: $temperature\n")
            requestInfo.append("Messages: ${conversationHistory.size}\n")
            requestInfo.append("\nRequest JSON:\n${jsonParser.encodeToString(ChatCompletionRequest.serializer(), request)}\n")

            val httpResponse = client.post(LMSTUDIO_API_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            requestInfo.append("\n=== RESPONSE INFO ===\n")
            requestInfo.append("Status: ${httpResponse.status}\n")

            val responseBody = httpResponse.body<String>()
            requestInfo.append("\nResponse JSON:\n$responseBody\n")

            val response: ChatCompletionResponse = jsonParser.decodeFromString(responseBody)

            CoreClientResponse(
                content = response.choices.firstOrNull()?.message?.content.orEmpty(),
                promptTokens = response.usage?.prompt_tokens,
                responseTokens = response.usage?.completion_tokens
            )
        } catch (e: Exception) {
            CoreClientResponse(
                content = errorMessage(
                    e,
                    requestInfo,
                    "Note: Make sure LM Studio is running and the local server is started on port 1234"
                )
            )
        }
    }

    override fun models(): List<String> {
        return listOf(
            "qwen/qwen2.5-coder-14b"
        )
    }

    fun close() {
        client.close()
    }
}
