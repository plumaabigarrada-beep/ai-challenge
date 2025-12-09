package org.example

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Enable raw mode for better terminal control
    try {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            Runtime.getRuntime().exec(arrayOf("sh", "-c", "stty raw -echo < /dev/tty"))
        }
    } catch (e: Exception) {
        // If we can't set raw mode, continue anyway
    }

    try {
        val ui = TerminalUI()
        ui.start()
    } finally {
        // Restore terminal
        try {
            val os = System.getProperty("os.name").lowercase()
            if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "stty sane < /dev/tty"))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
