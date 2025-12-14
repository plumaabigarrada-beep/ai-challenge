package com.jamycake.aiagent.data

import com.jamycake.aiagent.domain.FocusManager
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.user.User
import com.jamycake.aiagent.domain.slots.Users

internal class UsersImpl(
    private val chat: (ChatId) -> Chat?,
    private val focusManager: FocusManager
) : Users {

    override fun save(user: User) {

    }

    override fun get(): User {
        return User(
            chat = chat,
            focusManager = focusManager
        )
    }
}