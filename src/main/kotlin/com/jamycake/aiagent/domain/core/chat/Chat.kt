package com.jamycake.aiagent.domain.core.chat

internal class Chat(
    val id: ChatId = ChatId(),
    var name: String = "",
    messages: List<ChatMessage> = emptyList()
) {

    private val _messages = messages.toMutableList()
    val messages: List<ChatMessage> get() = _messages.toList()

    private val members: MutableMap<ChatMemberId, suspend (ChatMessage) -> Unit> = mutableMapOf()

    fun addMember(memberId: ChatMemberId = ChatMemberId(), listener: suspend (ChatMessage) -> Unit) : ChatMemberId {
        members[memberId] = listener
        return memberId
    }

    fun removeMember(memberId: ChatMemberId) {
        members.remove(memberId)
    }

    suspend fun sendMessage(senderId: ChatMemberId, message: ChatMessage) {
        _messages.add(message)
        members.forEach { (memberId, function) ->
            if (memberId != senderId) {
                function.invoke(message)
            }
        }
    }

}