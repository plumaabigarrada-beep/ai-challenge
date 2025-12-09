package org.example

/**
 * ANSI escape codes for terminal control
 */
object AnsiCodes {
    // Screen control
    const val CLEAR_SCREEN = "\u001B[2J"
    const val CLEAR_LINE = "\u001B[2K"
    const val HIDE_CURSOR = "\u001B[?25l"
    const val SHOW_CURSOR = "\u001B[?25h"
    const val RESET = "\u001B[0m"
    const val BOLD = "\u001B[1m"
    const val REVERSE_VIDEO = "\u001B[7m"

    // Colors
    const val CYAN = "\u001B[36m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val WHITE = "\u001B[37m"
    const val GRAY = "\u001B[90m"
    const val RED = "\u001B[31m"

    // Cursor positioning
    fun moveCursor(row: Int, col: Int) = "\u001B[${row};${col}H"
    fun saveCursor() = "\u001B[s"
    fun restoreCursor() = "\u001B[u"
}

/**
 * Terminal utility functions
 */
object TerminalUtils {
    /**
     * Get terminal size using multiple detection methods
     */
    fun getTerminalSize(currentHeight: Int = 24, currentWidth: Int = 80): Pair<Int, Int> {
        var newHeight = currentHeight
        var newWidth = currentWidth

        try {
            // Method 1: Try stty size
            try {
                val process = ProcessBuilder("sh", "-c", "stty size 2>/dev/null || stty size < /dev/tty 2>/dev/null")
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .start()

                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()

                if (output.isNotEmpty() && !output.contains("not")) {
                    val parts = output.split(" ")
                    if (parts.size == 2) {
                        newHeight = parts[0].toIntOrNull() ?: newHeight
                        newWidth = parts[1].toIntOrNull() ?: newWidth
                    }
                }
            } catch (e: Exception) {
                // Method 2: Try tput
                try {
                    val heightProc = ProcessBuilder("tput", "lines")
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                    val heightStr = heightProc.inputStream.bufferedReader().readText().trim()
                    heightProc.waitFor()
                    newHeight = heightStr.toIntOrNull() ?: newHeight

                    val widthProc = ProcessBuilder("tput", "cols")
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .start()
                    val widthStr = widthProc.inputStream.bufferedReader().readText().trim()
                    widthProc.waitFor()
                    newWidth = widthStr.toIntOrNull() ?: newWidth
                } catch (e2: Exception) {
                    // Method 3: Try environment variables
                    newHeight = System.getenv("LINES")?.toIntOrNull() ?: newHeight
                    newWidth = System.getenv("COLUMNS")?.toIntOrNull() ?: newWidth
                }
            }
        } catch (e: Exception) {
            // Keep current values
        }

        return Pair(newHeight.coerceAtLeast(10), newWidth.coerceAtLeast(60))
    }

    /**
     * Wrap text to fit within a maximum width, breaking on word boundaries
     */
    fun wrapText(text: String, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        text.split("\n").forEach { line ->
            if (line.isEmpty()) {
                lines.add("")
                return@forEach
            }

            val words = line.split(" ")
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (testLine.length <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }
                    currentLine = word
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
        }
        return lines
    }

    /**
     * Clean markdown formatting from text
     */
    fun cleanMarkdown(text: String): String {
        return text.lines().joinToString("\n") { line ->
            line
                .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
                .replace(Regex("""\*(.+?)\*"""), "$1")
                .replace(Regex("""^#{1,6}\s+"""), "")
                .replace(Regex("""^-\s+"""), "• ")
                .replace(Regex("""^---+$"""), "")
                .replace(Regex("""\[(.+?)\]\(.+?\)"""), "$1")
                .replace(Regex("""\[\d+\]"""), "")
        }
    }
}
