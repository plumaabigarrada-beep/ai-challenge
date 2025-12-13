package com.jamycake.aiagent.domain.core.chat

import java.util.*

data class ChatMemberId(
    val value: String = UUID.randomUUID().toString()
)