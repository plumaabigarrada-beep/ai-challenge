package com.jamycake.aiagent.domain.usecases

import com.jamycake.aiagent.app.App

internal class SendMessage(
    private val app: App
) {

    suspend fun invoke(text: String) {
//        app.core.user.sendMessage(message = text)
    }

}