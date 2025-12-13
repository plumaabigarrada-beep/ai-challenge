package com.jamycake.aiagent.terminal

import com.jamycake.aiagent.domain.slots.UI

class TerminalUI : UI {

    override fun out(message: String) {
        println(message)
    }
}