package org.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

const val HF_API_URL = "https://router.huggingface.co/v1/chat/completions"

@Serializable
data class HFMessage(
    val role: String,
    val content: String
)

@Serializable
data class HuggingFaceRequest(
    val messages: List<HFMessage>,
    val model: String,
    val stream: Boolean = false,
    val temperature: Double? = null
)

@Serializable
data class HFChoice(
    val message: HFMessage,
    val finish_reason: String? = null,
    val index: Int? = null
)

@Serializable
data class HFUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)

@Serializable
data class HuggingFaceResponse(
    val choices: List<HFChoice>,
    val usage: HFUsage? = null,
    val id: String? = null,
    val model: String? = null,
    val created: Long? = null
)

class HuggingFaceClient : Client {
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
            // Convert CoreMessage to HFMessage
            val messages = conversationHistory.map {
                HFMessage(role = it.role, content = it.content)
            }

            val request = HuggingFaceRequest(
                messages = messages,
                model = model,
                stream = false,
                temperature = temperature
            )

            requestInfo.append("=== REQUEST INFO ===\n")
            requestInfo.append("Model: $model\n")
            requestInfo.append("Temperature: $temperature\n")
            requestInfo.append("Messages: ${conversationHistory.size}\n")
            requestInfo.append("\nRequest JSON:\n${jsonParser.encodeToString(HuggingFaceRequest.serializer(), request)}\n")

            val httpResponse = client.post(HF_API_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $HUGGINGFACE_API_KEY")
                setBody(request)
            }

            requestInfo.append("\n=== RESPONSE INFO ===\n")
            requestInfo.append("Status: ${httpResponse.status}\n")

            val responseBody = httpResponse.body<String>()
            requestInfo.append("\nResponse JSON:\n$responseBody\n")

            val response: HuggingFaceResponse = jsonParser.decodeFromString(responseBody)

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
            "MiniMaxAI/MiniMax-M2:novita",
//            "allenai/Olmo-3-32B-Think:publicai",
            "deepseek-ai/DeepSeek-V3.2:novita",
            "Qwen/Qwen3-4B-Instruct-2507:nscale",
//            "meta-llama/Llama-3.2-3B-Instruct:nscale",
//            "Qwen/Qwen2.5-72B-Instruct:nscale",
//            "meta-llama/Llama-3.3-70B-Instruct:nscale",
//            "mistralai/Mixtral-8x7B-Instruct-v0.1:nscale",
//            "mistralai/Mistral-7B-Instruct-v0.3:nscale",
//            "NousResearch/Hermes-3-Llama-3.1-8B:nscale"
        )
    }

    fun close() {
        client.close()
    }
}
