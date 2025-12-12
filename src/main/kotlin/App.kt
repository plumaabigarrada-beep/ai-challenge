package org.example

import chatcontainer.ChatContainer
import client.Client
import commands.SendMessageCommand

internal class App(
    private val clients: Map<ClientType, Client>,
    val commands: List<Command>,
    val chatContainer: ChatContainer,
    val sendMessageCommand: SendMessageCommand
) {

    fun close(){
        clients.forEach { it.value.close() }
    }
}