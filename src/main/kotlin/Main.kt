package org.example

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


const val API_URL = "https://api.perplexity.ai/chat/completions"

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class PerplexityRequest(
    val model: String,
    val messages: List<Message>
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

suspend fun askPerplexity(query: String): String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    return try {
        val request = PerplexityRequest(
            model = "sonar-pro",
            messages = listOf(
                Message(role = "user", content = query)
            )
        )

        val response: PerplexityResponse = client.post(API_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $API_KEY")
            setBody(request)
        }.body()

        response.choices.firstOrNull()?.message?.content ?: "No response from AI"
    } catch (e: Exception) {
        "Error: ${e.message}"
    } finally {
        client.close()
    }
}

suspend fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: <query>")
        println("Example: What are the major AI developments today?")
        return
    }

    val query = args.joinToString(" ")
    println("Запрос: $query\n")
    println("Ожидание ответа...\n")

    val response = askPerplexity(query)
    println("Ответ AI:")
    println(response)
}