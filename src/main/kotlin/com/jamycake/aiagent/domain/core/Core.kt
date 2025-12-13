package com.jamycake.aiagent.domain.core

import com.jamycake.aiagent.domain.core.agent.Agent
import com.jamycake.aiagent.domain.core.chat.Chat
import com.jamycake.aiagent.domain.core.user.User

internal class Core(
    val agent: Agent,
    val user: User,
    val chat: Chat
)