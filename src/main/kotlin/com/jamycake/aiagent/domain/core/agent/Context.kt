package com.jamycake.aiagent.domain.core.agent

internal class Context(
    messages: List<ContextMessage>,
) {

    private val _messages = messages.toMutableList()
    val messages: List<ContextMessage> get() = _messages.toList()


    fun addMessage(message: ContextMessage) {
        _messages.add(message)
    }

    fun clear(){
        _messages.clear()
    }


}