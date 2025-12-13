package com.jamycake.aiagent.domain.core.agent

import com.jamycake.aiagent.domain.core.chat.ChatId

internal data class AgentState(
    val name: String,
    val config: Config,
    var chatId: ChatId,
    val context: Context
)