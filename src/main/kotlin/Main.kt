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

val jsonParser = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

val SYSTEM_PROMPT = """You are a character creation engine for a Dungeons & Dragons game. You build a character based on the following set of characteristics:

Height
Weight
Age
Race
Wings (yes/no)
The user must provide all the data. If even one of the items is missing, ask the user for the missing information.

Once all items are filled in, display the character's characteristics.

In your first message, request all characteristics at once.

If the user does not provide all characteristics at once, ask for them one by one with each message.

After all characteristics are provided, display them to the user and offer additional characteristics. Keep offering additional characteristics until the user says they are sufficient.

Only after the user explicitly finishes the process, display the final version with additional characteristics."""

suspend fun chatWithAI(conversationHistory: MutableList<Message>): String? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    return try {
        val request = PerplexityRequest(
            model = "sonar-pro",
            messages = conversationHistory
        )

        val response: PerplexityResponse = client.post(API_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $API_KEY")
            setBody(request)
        }.body()

        response.choices.firstOrNull()?.message?.content
    } catch (e: Exception) {
        println("╔════════════════════════════════════════════════════════════╗")
        println("║                        ОШИБКА                              ║")
        println("╠════════════════════════════════════════════════════════════╣")
        val errorMsg = "Ошибка: ${e.message}"
        val errorLines = wrapText(errorMsg, 58)
        errorLines.forEach { line ->
            println("║ ${padEndVisual(line, 58)} ║")
        }
        println("╚════════════════════════════════════════════════════════════╝")
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
    val lines = mutableListOf<String>()

    // Разбиваем текст на строки по символам новой строки
    text.split("\n").forEach { line ->
        if (line.isEmpty()) {
            lines.add("")
            return@forEach
        }

        val words = line.split(" ")
        var currentLine = ""

        for (word in words) {
            if (currentLine.isEmpty()) {
                currentLine = word
            } else {
                val testLine = "$currentLine $word"
                if (getVisualWidth(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    lines.add(currentLine)
                    currentLine = word
                }
            }

            // Если слово само по себе длиннее maxWidth, разбиваем его
            if (getVisualWidth(currentLine) > maxWidth) {
                var remaining = currentLine
                while (getVisualWidth(remaining) > maxWidth) {
                    var splitPoint = maxWidth
                    while (splitPoint > 0 && getVisualWidth(remaining.substring(0, splitPoint)) > maxWidth) {
                        splitPoint--
                    }
                    if (splitPoint > 0) {
                        lines.add(remaining.substring(0, splitPoint))
                        remaining = remaining.substring(splitPoint)
                    } else {
                        break
                    }
                }
                currentLine = remaining
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
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

fun cleanMarkdown(text: String): String {
    return text.lines().joinToString("\n") { line ->
        line
            // Убираем жирный текст **text** -> text
            .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
            // Убираем курсив *text* -> text
            .replace(Regex("""\*(.+?)\*"""), "$1")
            // Убираем заголовки ##
            .replace(Regex("""^#{1,6}\s+"""), "")
            // Убираем маркеры списков - в начале строки
            .replace(Regex("""^-\s+"""), "• ")
            // Убираем горизонтальные линии
            .replace(Regex("""^---+$"""), "")
            // Убираем ссылки [text](url) -> text
            .replace(Regex("""\[(.+?)\]\(.+?\)"""), "$1")
            // Убираем ссылки на источники [1], [2] и т.д.
            .replace(Regex("""\[\d+\]"""), "")
    }
}

fun displayMessage(role: String, content: String) {
    val boxWidth = 60
    val contentWidth = boxWidth - 2

    println("\n╔${"═".repeat(boxWidth)}╗")

    val header = if (role == "assistant") "🎲 AI Мастер" else "👤 Вы"
    val headerWidth = getVisualWidth(header)
    val headerPadding = (boxWidth - headerWidth) / 2
    println("║${" ".repeat(headerPadding)}$header${" ".repeat(boxWidth - headerWidth - headerPadding)}║")

    println("╠${"═".repeat(boxWidth)}╣")

    // Очищаем markdown перед обработкой
    val cleanContent = cleanMarkdown(content)
    val contentLines = wrapText(cleanContent, contentWidth)

    contentLines.forEach { line ->
        println("║ ${padEndVisual(line, contentWidth)} ║")
    }

    println("╚${"═".repeat(boxWidth)}╝")
}

fun displayWelcome() {
    val boxWidth = 60
    val contentWidth = boxWidth - 2

    println("\n╔${"═".repeat(boxWidth)}╗")

    val header = "D&D CHARACTER CREATOR"
    val headerWidth = getVisualWidth(header)
    val headerPadding = (boxWidth - headerWidth) / 2
    println("║${" ".repeat(headerPadding)}$header${" ".repeat(boxWidth - headerWidth - headerPadding)}║")

    println("╠${"═".repeat(boxWidth)}╣")

    val welcomeText = "Добро пожаловать в создание персонажа!"
    val welcomeLines = wrapText(welcomeText, contentWidth)
    welcomeLines.forEach { line ->
        println("║ ${padEndVisual(line, contentWidth)} ║")
    }

    println("║${" ".repeat(boxWidth)}║")

    val instructionText = "Введите 'выход' или 'quit' чтобы завершить."
    val instructionLines = wrapText(instructionText, contentWidth)
    instructionLines.forEach { line ->
        println("║ ${padEndVisual(line, contentWidth)} ║")
    }

    println("╚${"═".repeat(boxWidth)}╝\n")
}

suspend fun main() {
    displayWelcome()

    // Инициализируем историю разговора с системным промптом
    val conversationHistory = mutableListOf(
        Message(role = "system", content = SYSTEM_PROMPT)
    )

    // Получаем первое сообщение от AI
    println("Инициализация AI Мастера...\n")

    // Добавляем пустое сообщение пользователя для начала диалога
    conversationHistory.add(Message(role = "user", content = "Привет! Я хочу создать персонажа для D&D."))

    val firstResponse = chatWithAI(conversationHistory)
    if (firstResponse != null) {
        conversationHistory.add(Message(role = "assistant", content = firstResponse))
        displayMessage("assistant", firstResponse)
    } else {
        println("Ошибка инициализации. Попробуйте перезапустить программу.")
        return
    }

    // Основной цикл чата
    while (true) {
        print("\n> ")
        val userInput = safeReadLine().trim()

        if (userInput.isEmpty()) {
            println("⚠ Введите сообщение или 'выход' для завершения.")
            continue
        }

        // Проверка на выход
        if (userInput.lowercase() in listOf("выход", "quit", "exit", "q")) {
            println("\n╔════════════════════════════════════════════════════════════╗")
            println("║              Спасибо за использование!                     ║")
            println("║           Удачи в ваших приключениях! 🎲                   ║")
            println("╚════════════════════════════════════════════════════════════╝\n")
            break
        }

        // Отображаем сообщение пользователя
        displayMessage("user", userInput)

        // Добавляем сообщение пользователя в историю
        conversationHistory.add(Message(role = "user", content = userInput))

        // Получаем ответ от AI
        println("\n⏳ AI Мастер думает...")
        val aiResponse = chatWithAI(conversationHistory)

        if (aiResponse != null) {
            // Добавляем ответ AI в историю
            conversationHistory.add(Message(role = "assistant", content = aiResponse))
            // Отображаем ответ
            displayMessage("assistant", aiResponse)
        } else {
            println("⚠ Не удалось получить ответ. Попробуйте еще раз.")
        }
    }
}