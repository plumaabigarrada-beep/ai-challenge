package com.jamycake.aiagent.chatsaver

import com.jamycake.aiagent.chat.Chat
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class ChatSaver {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun saveChat(chat: Chat, directory: String = "saved_chats"): String {
        return try {
            // Create directory if it doesn't exist
            val dir = File(directory)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            // Generate filename with timestamp
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val sanitizedName = chat.name.replace(Regex("[^a-zA-Z0-9-_]"), "_")
            val filename = "${sanitizedName}_${timestamp}.json"
            val filePath = File(dir, filename)

            // Create serializable chat data
            val chatData = SavedChatData(
                id = chat.id,
                name = chat.name,
                conversationHistory = chat.getConversationHistory().map {
                    SerializableMessage(
                        role = it.role,
                        content = it.content,
                        tokens = it.tokens,
                        durationMs = it.durationMs
                    )
                },
                savedAt = LocalDateTime.now().toString()
            )

            // Write to file
            val jsonString = json.encodeToString(chatData)
            filePath.writeText(jsonString)

            "Chat saved successfully to: ${filePath.absolutePath}\n"
        } catch (e: Exception) {
            "Error saving chat: ${e.message}\n"
        }
    }

    fun loadChat(filePath: String): SavedChatData? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return null
            }
            val jsonString = file.readText()
            json.decodeFromString<SavedChatData>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class SavedChatData(
    val id: String,
    val name: String,
    val conversationHistory: List<SerializableMessage>,
    val savedAt: String
)

@Serializable
data class SerializableMessage(
    val role: String,
    val content: String,
    val tokens: Int? = null,
    val durationMs: Long? = null
)