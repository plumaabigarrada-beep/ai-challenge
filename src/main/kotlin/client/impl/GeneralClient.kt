package org.example

import client.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.example.chat.ChatMessage
import org.example.context.Context
import org.example.context.ContextMessage

const val LMSTUDIO_API_URL = "http://localhost:1234/v1/chat/completions"

internal class GeneralClient(
    val baseUrl: String,
) : Client {
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

    override suspend fun sendContext(
        context: Context,
        temperature: Double,
        model: String,
    ): Pair<Context, ChatMessage> {
        // Convert ContextMessage to ResponseChatMessage
        val messages = context.messages.map {
            ResponseChatMessage(role = it.role, content = it.content)
        }

        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            temperature = temperature
        )

        val httpResponse = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val responseBody = httpResponse.body<String>()
        val response: ChatCompletionResponse = jsonParser.decodeFromString(responseBody)

        // Extract AI response messages as ContextMessages
        val newContextMessages = response.choices.map {
            ContextMessage(
                role = it.message.role,
                content = it.message.content
            )
        }

        val responseContext = Context(
            messages = newContextMessages,
        )

        // Get the first choice's message and usage
        val firstChoice = response.choices.firstOrNull()
        val chatMessage = if (firstChoice != null) {
            ChatMessage(
                role = firstChoice.message.role,
                message = firstChoice.message.content,
                usage = response.usage?.let {
                    chat.ChatUsage(
                        prompt_tokens = it.prompt_tokens,
                        completion_tokens = it.completion_tokens,
                        total_tokens = it.total_tokens
                    )
                }
            )
        } else {
            ChatMessage(role = "assistant", message = "", usage = null)
        }

        return Pair(responseContext, chatMessage)
    }

    override fun models(): List<String> {
        return listOf(
            "qwen/qwen3-coder-30b"
        )
    }

    override fun close() {
        client.close()
    }
}
