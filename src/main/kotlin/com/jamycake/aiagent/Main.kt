package com.jamycake.aiagent

import com.jamycake.aiagent.app.createApp
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {


    val app = createApp()

    app.run()

    app.close()
}

