package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMessage
import com.jamycake.aiagent.domain.slots.Chats
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class ChatsImpl(
    private val storagePath: String = "chats/"
) : Chats {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun getChat(): Chat {
        val chatId = ChatId.empty()
        val chat = Chat(id = chatId)
        return chat
    }

    override suspend fun saveChat(chat: Chat) {
        val savedMessages = chat.messages.map { msg ->
            SavedChatMessage(
                id = msg.id,
                name = msg.name,
                role = msg.role,
                content = msg.content,
                contextMessageId = msg.contextMessageId
            )
        }

        val savedData = SavedChatData(
            id = chat.id.value,
            name = chat.name,
            messages = savedMessages
        )

        val jsonString = json.encodeToString(savedData)

        // Ensure the directory exists
        val chatFolder = File(storagePath)
        if (!chatFolder.exists()) {
            chatFolder.mkdirs()
        }

        // Write to a specific file using chat's ID
        File(storagePath, "${chat.id.value}.json").writeText(jsonString)
    }

    override suspend fun getAllChats(): List<Chat> {
        val chatFolder = File(storagePath)
        val listFiles = chatFolder.listFiles()
        if (!chatFolder.exists() || listFiles.isEmpty()) {
            return emptyList()
        }

        return listFiles.map { file ->
            val savedData = json.decodeFromString<SavedChatData>(file.readText())

            val messages = savedData.messages.map { msg ->
                ChatMessage(
                    id = msg.id,
                    name = msg.name,
                    role = msg.role,
                    content = msg.content,
                    contextMessageId = msg.contextMessageId
                )
            }

            Chat(
                id = ChatId(savedData.id),
                name = savedData.name,
                messages = messages
            )
        }
    }

    override suspend fun newChat(): Chat {
        return Chat()
    }

    override suspend fun deleteChat(chat: Chat) {
        val chatFile = File(storagePath, "${chat.id.value}.json")
        if (chatFile.exists()) {
            chatFile.delete()
        }
    }

    @Serializable
    private data class SavedChatData(
        val id: String,
        val name: String = "",
        val messages: List<SavedChatMessage>
    )

    @Serializable
    private data class SavedChatMessage(
        val id: String,
        val name: String,
        val role: String,
        val content: String,
        val contextMessageId: String? = null
    )
}