package com.jamycake.aiagent

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {


    val app = createApp()

    app.run()

    app.close()
}

