package org.example

interface Client {

    suspend fun sendMessage(
        conversationHistory: List<Message>,
        temperature: Double,
        model: String,
    ) : String

    fun models() : List<String>

}