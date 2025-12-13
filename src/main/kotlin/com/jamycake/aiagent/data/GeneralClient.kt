package com.jamycake.aiagent.data


import com.jamycake.aiagent.domain.core.agent.Context
import com.jamycake.aiagent.domain.core.agent.ContextMessage
import com.jamycake.aiagent.domain.core.agent.TokensUsage
import com.jamycake.aiagent.domain.slots.Client
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

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
    ): Pair<ContextMessage, TokensUsage> {
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


        // Get the first choice's message and usage
        val firstChoice = response.choices.first()

        val contextMessage = ContextMessage(
            role = firstChoice.message.role,
            content = firstChoice.message.content
        )
        val tokensUsage = TokensUsage(
            prompt_tokens = response.usage?.prompt_tokens,
            completion_tokens = response.usage?.completion_tokens,
            total_tokens = response.usage?.total_tokens
        )


        val result = Pair(contextMessage, tokensUsage)

        return result
    }

    override fun models(): List<String> {
        return listOf(
            "qwen/qwen2.5-coder-14b"
        )
    }

    override fun close() {
        client.close()
    }
}