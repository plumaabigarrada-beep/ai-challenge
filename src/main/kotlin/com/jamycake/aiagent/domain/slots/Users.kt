package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.user.UserState

internal interface Users {


    fun save(user: UserState)

    fun get() : UserState

}