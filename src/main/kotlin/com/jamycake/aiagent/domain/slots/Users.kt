package com.jamycake.aiagent.domain.slots

import com.jamycake.aiagent.domain.core.user.User

internal interface Users {


    fun save(user: User)

    fun get() : User

}