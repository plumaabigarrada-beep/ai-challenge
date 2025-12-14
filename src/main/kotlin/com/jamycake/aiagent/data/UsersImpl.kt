package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId
import com.jamycake.aiagent.domain.core.user.UserId
import com.jamycake.aiagent.domain.core.user.UserState
import com.jamycake.aiagent.domain.slots.Users
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal class UsersImpl(
    private val storagePath: String = "users/"
) : Users {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val userFileName = "user.json"

    override fun save(user: UserState) {
        val savedData = SavedUserData(
            id = user.id.value,
            chatId = user.chatId?.value.orEmpty(),
            chatMemberId = user.chatMemberId?.value.orEmpty()
        )

        val jsonString = json.encodeToString(savedData)

        val userFolder = File(storagePath)
        if (!userFolder.exists()) {
            userFolder.mkdirs()
        }

        File(storagePath, userFileName).writeText(jsonString)
    }

    override fun get(): UserState {
        val userFile = File(storagePath, userFileName)

        if (!userFile.exists()) {
            return UserState(
                id = UserId(),
                chatId = null,
                chatMemberId = null
            )
        }

        val savedData = json.decodeFromString<SavedUserData>(userFile.readText())

        return UserState(
            id = UserId(savedData.id),
            chatId = if (savedData.chatId.isEmpty()) null else ChatId(savedData.chatId),
            chatMemberId = if (savedData.chatMemberId.isEmpty()) null else ChatMemberId(savedData.chatMemberId)
        )
    }

    @Serializable
    private data class SavedUserData(
        val id: String,
        val chatId: String,
        val chatMemberId: String
    )
}