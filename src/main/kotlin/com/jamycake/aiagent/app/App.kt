package com.jamycake.aiagent.app

import com.jamycake.aiagent.domain.core.agent.ClientType
import com.jamycake.aiagent.domain.slots.Client
import com.jamycake.aiagent.terminal.Terminal

internal class App(
    private val clients: Map<ClientType, Client>,
    private val terminal: Terminal
) {

    fun close() {
        clients.forEach { _, client -> client.close() }
    }


    suspend fun run(){

        terminal.run()
    }

}