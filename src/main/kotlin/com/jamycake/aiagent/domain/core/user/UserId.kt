package com.jamycake.aiagent.domain.core.user

import java.util.*

data class UserId(
    val value: String = UUID.randomUUID().toString()
) {

    companion object {
        fun empty() : UserId {
            return UserId("")
        }
    }

}