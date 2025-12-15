package com.jamycake.aiagent.data


import com.jamycake.aiagent.domain.core.agent.Context
import com.jamycake.aiagent.domain.core.agent.ContextMessage
import com.jamycake.aiagent.domain.core.agent.TokensUsage
import com.jamycake.aiagent.domain.core.agent.ToolCall
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.domain.slots.ClientResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

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
        systemPrompt: String,
        tools: List<Map<String, Any>>?
    ): ClientResponse {
        // Convert ContextMessage to ResponseChatMessage
        val simpleMessages = context.messages.map {
            when (it.role) {
                "tool" -> ResponseChatMessage(
                    role = it.role,
                    content = it.content,
                    tool_call_id = it.toolCallId
                )
                else -> ResponseChatMessage(
                    role = it.role,
                    content = if (it.toolCalls != null) "" else it.content,
                    tool_calls = it.toolCalls?.map { tc ->
                        ApiToolCall(
                            id = tc.id,
                            type = "function",
                            function = ToolCallFunction(
                                name = tc.name,
                                arguments = Json.encodeToString(
                                    MapSerializer(String.serializer(), String.serializer()),
                                    tc.arguments
                                )
                            )
                        )
                    }
                )
            }
        }

        val messages = if (systemPrompt.isEmpty()) {
            simpleMessages
        } else {
            listOf(ResponseChatMessage(role = "system", content = systemPrompt)) + simpleMessages
        }

        val toolsJson = tools?.let { toolsList ->
            buildJsonArray {
                toolsList.forEach { toolMap ->
                    add(mapToJsonElement(toolMap))
                }
            }
        }

        val request = ChatCompletionRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            tools = toolsJson
        )

        val httpResponse = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val responseBody = httpResponse.body<String>()
        val response: ChatCompletionResponse = jsonParser.decodeFromString(responseBody)

        // Get the first choice's message and usage
        val firstChoice = response.choices.first()

        val tokensUsage = TokensUsage(
            prompt_tokens = response.usage?.prompt_tokens,
            completion_tokens = response.usage?.completion_tokens,
            total_tokens = response.usage?.total_tokens
        )

        // Parse tool calls if present
        val toolCalls = if (firstChoice.message.tool_calls != null && firstChoice.message.tool_calls.isNotEmpty()) {
            firstChoice.message.tool_calls.map { apiToolCall ->
                ToolCall(
                    id = apiToolCall.id,
                    name = apiToolCall.function.name,
                    arguments = Json.decodeFromString(
                        MapSerializer(String.serializer(), String.serializer()),
                        apiToolCall.function.arguments
                    )
                )
            }
        } else {
            emptyList()
        }

        val contextMessage = ContextMessage(
            role = firstChoice.message.role,
            content = firstChoice.message.content ?: "",
            toolCalls = toolCalls.takeIf { it.isNotEmpty() }
        )

        return ClientResponse(
            message = contextMessage,
            usage = tokensUsage,
            toolCalls = toolCalls
        )
    }

    override fun models(): List<String> {
        return listOf(
            "qwen/qwen2.5-coder-14b"
        )
    }

    override fun close() {
        client.close()
    }

    private fun mapToJsonElement(map: Map<String, Any>): JsonElement {
        return buildJsonObject {
            map.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, value)
                    is Boolean -> put(key, value)
                    is Map<*, *> -> put(key, mapToJsonElement(value as Map<String, Any>))
                    is List<*> -> put(key, buildJsonArray {
                        value.forEach { item ->
                            when (item) {
                                is String -> add(item)
                                is Number -> add(item)
                                is Boolean -> add(item)
                                is Map<*, *> -> add(mapToJsonElement(item as Map<String, Any>))
                                else -> add(JsonNull)
                            }
                        }
                    })
                    else -> put(key, JsonNull)
                }
            }
        }
    }
}