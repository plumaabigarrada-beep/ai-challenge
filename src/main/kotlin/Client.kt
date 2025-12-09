package org.example

interface Client {

    suspend fun sendMessage(
        conversationHistory: List<CoreMessage>,
        temperature: Double,
        model: String,
    ) : CoreClientResponse

    fun models() : List<String>

}