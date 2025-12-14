package com.jamycake.aiagent.domain.core.user

import com.jamycake.aiagent.domain.core.chat.ChatId
import com.jamycake.aiagent.domain.core.chat.ChatMemberId


data class UserState(
    val id: UserId,
    val chatId: ChatId?,
    val chatMemberId: ChatMemberId?,
)