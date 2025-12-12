package com.jamycake.aiagent.terminal.app

import com.jamycake.aiagent.chatcontainer.ChatContainer
import com.jamycake.aiagent.client.Client
import com.jamycake.aiagent.terminal.commands.SendMessageCommand
import org.example.ClientType
import com.jamycake.aiagent.terminal.Command

internal class TerminalApp(
    private val clients: Map<ClientType, Client>,
    val commands: List<Command>,
    val chatContainer: ChatContainer,
    val sendMessageCommand: SendMessageCommand
) {

    fun close(){
        clients.forEach { it.value.close() }
    }
}