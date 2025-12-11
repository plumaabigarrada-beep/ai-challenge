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

const val HF_API_URL = "https://router.huggingface.co/v1/chat/completions"

class HuggingFaceClient : Client {
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
                messages = messages,
                model = model,
                stream = false,
                temperature = temperature
            )

            requestInfo.append("=== REQUEST INFO ===\n")
            requestInfo.append("Model: $model\n")
            requestInfo.append("Temperature: $temperature\n")
            requestInfo.append("Messages: ${conversationHistory.size}\n")
            requestInfo.append("\nRequest JSON:\n${jsonParser.encodeToString(ChatCompletionRequest.serializer(), request)}\n")

            val httpResponse = client.post(HF_API_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $HUGGINGFACE_API_KEY")
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
            CoreClientResponse(content = errorMessage(e, requestInfo))
        }
    }

    override fun models(): List<String> {
        return listOf(
            "MiniMaxAI/MiniMax-M2:novita",
            "deepseek-ai/DeepSeek-V3.2:novita",
            "Qwen/Qwen3-4B-Instruct-2507:nscale",
//            "Qwen/Qwen2.5-72B-Instruct:nscale",
//            "meta-llama/Llama-3.3-70B-Instruct:nscale",
//            "mistralai/Mixtral-8x7B-Instruct-v0.1:nscale",
//            "mistralai/Mistral-7B-Instruct-v0.3:nscale",
//            "NousResearch/Hermes-3-Llama-3.1-8B:nscale"
        )
    }

    override fun close() {
        client.close()
    }
}
