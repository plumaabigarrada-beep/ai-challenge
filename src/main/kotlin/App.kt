package org.example

import chat.Chat
import chatcontainer.ChatContainer
import client.Client
import compressor.ChatCompressor
import chatsaver.ChatSaver
import commands.*

class App(
    private val clients: Map<ClientType, Client>,
    val commands: List<Command>,
    val chatContainer: ChatContainer,
    val sendMessageCommand: SendMessageCommand
) {

    fun close(){
        clients.forEach { it.value.close() }
    }
}