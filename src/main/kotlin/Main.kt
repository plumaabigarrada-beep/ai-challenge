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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

const val API_URL = "https://api.perplexity.ai/chat/completions"

val inputReader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String,
    @SerialName("json_schema")
    val jsonSchema: JsonSchema
)

@Serializable
data class JsonSchema(
    val schema: JsonObject
)

@Serializable
data class PerplexityRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null
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

@Serializable
data class GoodNews(
    val title: String,
    val content: String,
    val date: String
)

val jsonParser = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

suspend fun askPerplexity(query: String): String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
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

suspend fun getGoodNews(): GoodNews? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    return try {
        val request = PerplexityRequest(
            model = "sonar-pro",
            messages = listOf(
                Message(
                    role = "system",
                    content = "DO NOT use markdown"
                ),
                Message(
                    role = "user",
                    content = """Please find a good news.

Send it in clear json format to me.
DO NOT use markdown
The message should start from "{" and end by "}"
json must have ONLY title, date, and content fields
text should translate to russian
"""
                )
            )
        )

        val response: PerplexityResponse = client.post(API_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $API_KEY")
            setBody(request)
        }.body()

        val rawContent = response.choices.firstOrNull()?.message?.content ?: return null

        // Убираем markdown обертку ```json ... ```
        val jsonContent = rawContent.trim().let { content ->
            when {
//                content.startsWith("```json") && content.endsWith("```") -> {
//                    content.removePrefix("```json").removeSuffix("```").trim()
//                }
//                content.startsWith("```") && content.endsWith("```") -> {
//                    content.removePrefix("```").removeSuffix("```").trim()
//                }
                else -> content
            }
        }

        try {
            jsonParser.decodeFromString<GoodNews>(jsonContent)
        } catch (e: Exception) {
            println("╔════════════════════════════════════════════════════════════╗")
            println("║                  ОШИБКА ПАРСИНГА JSON                      ║")
            println("╠════════════════════════════════════════════════════════════╣")
            println("║ Не удалось распарсить ответ от Perplexity.                ║")
            println("║ Ошибка: ${e.message?.take(44)?.padEnd(44)}║")
            println("╠════════════════════════════════════════════════════════════╣")
            println("║ Сырой ответ:                                              ║")
            println("╠════════════════════════════════════════════════════════════╣")

            // Выводим сырой ответ построчно
            jsonContent.lines().forEach { line ->
                if (line.length <= 58) {
                    println("║ ${line.padEnd(58)} ║")
                } else {
                    // Если строка слишком длинная, разбиваем её
                    line.chunked(58).forEach { chunk ->
                        println("║ ${chunk.padEnd(58)} ║")
                    }
                }
            }

            println("╚════════════════════════════════════════════════════════════╝")
            null
        }
    } catch (e: Exception) {
        println("Ошибка получения новости: ${e.message}")
        null
    } finally {
        client.close()
    }
}

fun safeReadLine(): String {
    return try {
        inputReader.readLine() ?: ""
    } catch (e: java.nio.charset.MalformedInputException) {
        println("⚠ Ошибка кодировки ввода. Пожалуйста, используйте UTF-8.")
        ""
    } catch (e: Exception) {
        println("⚠ Ошибка чтения ввода: ${e.message}")
        ""
    }
}

fun isPositiveAnswer(answer: String): Boolean {
    val positive = listOf("да", "yes", "конечно", "давай", "хочу", "ага", "угу", "ок", "okay", "+", "д", "y")
    return positive.any { answer.lowercase().trim().startsWith(it) }
}

fun getVisualWidth(text: String): Int {
    return text.codePoints().map { cp ->
        when {
            cp in 0x1F300..0x1F9FF -> 2 // Эмодзи
            cp in 0x2600..0x26FF -> 2   // Разные символы
            cp in 0x2700..0x27BF -> 2   // Дингбаты
            cp > 0x1F000 -> 2           // Другие широкие символы
            else -> 1
        }
    }.sum()
}

fun wrapText(text: String, maxWidth: Int): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        if (currentLine.isEmpty()) {
            currentLine = word
        } else if (getVisualWidth(currentLine) + getVisualWidth(word) + 1 <= maxWidth) {
            currentLine += " $word"
        } else {
            lines.add(currentLine)
            currentLine = word
        }
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }
    return lines
}

fun padEndVisual(text: String, targetWidth: Int): String {
    val currentWidth = getVisualWidth(text)
    val spacesNeeded = targetWidth - currentWidth
    return if (spacesNeeded > 0) {
        text + " ".repeat(spacesNeeded)
    } else {
        text
    }
}

fun displayGoodNews(news: GoodNews) {
    val boxWidth = 60
    val contentWidth = boxWidth - 2 // Учитываем два пробела: после "║ " и перед " ║"

    println("\n╔${"═".repeat(boxWidth)}╗")

    val header = "ХОРОШАЯ НОВОСТЬ?"
    val headerWidth = getVisualWidth(header)
    val headerPadding = (boxWidth - headerWidth) / 2
    println("║${" ".repeat(headerPadding)}$header${" ".repeat(boxWidth - headerWidth - headerPadding)}║")

    println("╠${"═".repeat(boxWidth)}╣")

    // Заголовок
    val titleLines = wrapText(news.title, contentWidth)
    titleLines.forEach { line ->
        println("║ ${padEndVisual(line, contentWidth)} ║")
    }

    println("║${" ".repeat(boxWidth)}║")

    // Дата
    println("║ ${padEndVisual(news.date, contentWidth)} ║")

    println("║${" ".repeat(boxWidth)}║")

    // Контент
    val contentLines = wrapText(news.content, contentWidth)
    contentLines.forEach { line ->
        println("║ ${padEndVisual(line, contentWidth)} ║")
    }

    println("╚${"═".repeat(boxWidth)}╝\n")
}

suspend fun main() {



    val boxWidth = 60
    val welcomeText = "Добро пожаловать в AI Chat Assistant!"
    val welcomePadding = (boxWidth - welcomeText.length) / 2

    println("╔${"═".repeat(boxWidth)}╗")
    println("║${" ".repeat(welcomePadding)}$welcomeText${" ".repeat(boxWidth - welcomeText.length - welcomePadding)}║")
    println("╚${"═".repeat(boxWidth)}╝\n")

    print("Хотите услышать что-то хорошее? ")
    val answer = safeReadLine().trim()

    if (isPositiveAnswer(answer)) {
        println("\nОтлично! Ищу хорошую новость...\n")
        val news = getGoodNews()

        if (news != null) {
            displayGoodNews(news)
        } else {
            println("К сожалению, не удалось получить новость. Попробуйте еще раз.\n")
        }
    } else {
        print("^_^ Тогда введите запрос: ")
        val query = safeReadLine().trim()

        if (query.isNotEmpty()) {
            println("\nОбрабатываю запрос...\n")
            val response = askPerplexity(query)
            println("Ответ AI:")
            println(response)
        } else {
            println("Запрос не может быть пустым!")
        }
    }
}