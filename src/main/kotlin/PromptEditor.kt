package org.example

/**
 * Manages the system prompt editor with cursor navigation and text editing
 */
class PromptEditor(private val contentWidth: () -> Int) {
    private val lines = mutableListOf<String>()
    var cursorRow = 0
    var cursorCol = 0
    var scrollOffset = 0

    val selection = TextSelection()

    fun loadText(text: String) {
        lines.clear()
        lines.addAll(text.split("\n"))
        cursorRow = 0
        cursorCol = 0
        scrollOffset = 0
        selection.clear()
    }

    fun getText(): String = lines.joinToString("\n")

    fun getLines(): List<String> = lines

    fun getLinesForEditing(): MutableList<String> = lines

    /**
     * Calculate total wrapped lines for scrolling
     */
    fun calculateTotalWrappedLines(): Int {
        var total = 0
        for (line in lines) {
            val wrapped = TerminalUtils.wrapText(line, contentWidth())
            total += wrapped.size.coerceAtLeast(1)
        }
        return total
    }

    /**
     * Calculate display row for cursor accounting for wrapped lines
     */
    fun getDisplayRowForCursor(): Int {
        var displayRow = 0

        // Count wrapped lines before cursor line
        for (lineIndex in 0 until cursorRow) {
            displayRow += TerminalUtils.wrapText(lines.getOrElse(lineIndex) { "" }, contentWidth()).size.coerceAtLeast(1)
        }

        // Add wrapped segment within current line
        val currentLine = lines.getOrElse(cursorRow) { "" }
        val wrappedSegments = TerminalUtils.wrapText(currentLine, contentWidth())

        var charsProcessed = 0
        for ((idx, segment) in wrappedSegments.withIndex()) {
            if (cursorCol <= charsProcessed + segment.length) {
                displayRow += idx
                break
            }
            charsProcessed += segment.length
        }

        return displayRow
    }

    /**
     * Auto-scroll to keep cursor visible
     */
    fun autoScroll(visibleHeight: Int) {
        val displayRow = getDisplayRowForCursor()

        if (displayRow < scrollOffset) {
            scrollOffset = displayRow
        } else if (displayRow >= scrollOffset + visibleHeight) {
            scrollOffset = (displayRow - visibleHeight + 1).coerceAtLeast(0)
        }
    }

    /**
     * Update scroll offset constraints
     */
    fun updateScrollConstraints(visibleHeight: Int) {
        val totalWrappedLines = calculateTotalWrappedLines()
        val maxScroll = (totalWrappedLines - visibleHeight).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(0, maxScroll)
    }

    // ===== Cursor Movement =====

    fun moveCursorUp(visibleHeight: Int) {
        val width = contentWidth()
        val currentLine = lines.getOrElse(cursorRow) { "" }
        val wrappedSegments = TerminalUtils.wrapText(currentLine, width)

        // Find which wrapped segment we're currently on
        var charsInSegments = 0
        var currentSegment = 0
        for ((idx, segment) in wrappedSegments.withIndex()) {
            if (cursorCol <= charsInSegments + segment.length) {
                currentSegment = idx
                break
            }
            charsInSegments += segment.length
        }

        if (currentSegment > 0) {
            // Move up within same logical line
            val prevSegmentStart = wrappedSegments.take(currentSegment - 1).sumOf { it.length }
            val prevSegmentLen = wrappedSegments[currentSegment - 1].length
            val colWithinCurrentSegment = cursorCol - charsInSegments
            cursorCol = prevSegmentStart + colWithinCurrentSegment.coerceAtMost(prevSegmentLen)
        } else if (cursorRow > 0) {
            // Move to previous logical line's last wrapped segment
            cursorRow--
            val prevLine = lines[cursorRow]
            val prevWrapped = TerminalUtils.wrapText(prevLine, width)
            val lastSegmentStart = prevWrapped.dropLast(1).sumOf { it.length }
            val lastSegmentLen = prevWrapped.lastOrNull()?.length ?: 0
            val colWithinCurrentSegment = cursorCol - charsInSegments
            cursorCol = lastSegmentStart + colWithinCurrentSegment.coerceAtMost(lastSegmentLen)
        }

        autoScroll(visibleHeight)
    }

    fun moveCursorDown(visibleHeight: Int) {
        val width = contentWidth()
        val currentLine = lines.getOrElse(cursorRow) { "" }
        val wrappedSegments = TerminalUtils.wrapText(currentLine, width)

        // Find which wrapped segment we're currently on
        var charsInSegments = 0
        var currentSegment = 0
        for ((idx, segment) in wrappedSegments.withIndex()) {
            if (cursorCol <= charsInSegments + segment.length) {
                currentSegment = idx
                break
            }
            charsInSegments += segment.length
        }

        if (currentSegment < wrappedSegments.size - 1) {
            // Move down within same logical line
            val nextSegmentStart = wrappedSegments.take(currentSegment + 1).sumOf { it.length }
            val nextSegmentLen = wrappedSegments[currentSegment + 1].length
            val colWithinCurrentSegment = cursorCol - charsInSegments
            cursorCol = nextSegmentStart + colWithinCurrentSegment.coerceAtMost(nextSegmentLen)
        } else if (cursorRow < lines.size - 1) {
            // Move to next logical line's first wrapped segment
            cursorRow++
            val nextLine = lines[cursorRow]
            val nextWrapped = TerminalUtils.wrapText(nextLine, width)
            val firstSegmentLen = nextWrapped.firstOrNull()?.length ?: 0
            val colWithinCurrentSegment = cursorCol - charsInSegments
            cursorCol = colWithinCurrentSegment.coerceAtMost(firstSegmentLen)
        }

        autoScroll(visibleHeight)
    }

    fun moveCursorLeft(visibleHeight: Int) {
        val width = contentWidth()
        val currentLine = lines.getOrElse(cursorRow) { "" }
        val wrappedSegments = TerminalUtils.wrapText(currentLine, width)

        if (cursorCol > 0) {
            var charsInSegments = 0
            var currentSegment = 0
            for ((idx, segment) in wrappedSegments.withIndex()) {
                if (cursorCol <= charsInSegments + segment.length) {
                    currentSegment = idx
                    break
                }
                charsInSegments += segment.length
            }

            val colWithinSegment = cursorCol - charsInSegments

            if (colWithinSegment == 0 && currentSegment > 0) {
                // Move to end of previous segment
                val prevSegmentStart = wrappedSegments.take(currentSegment - 1).sumOf { it.length }
                val prevSegmentLen = wrappedSegments[currentSegment - 1].length
                cursorCol = prevSegmentStart + prevSegmentLen
            } else {
                cursorCol--
            }
        } else if (cursorRow > 0) {
            // Move to end of previous logical line
            cursorRow--
            cursorCol = lines.getOrElse(cursorRow) { "" }.length
        }

        autoScroll(visibleHeight)
    }

    fun moveCursorRight(visibleHeight: Int) {
        val width = contentWidth()
        val currentLine = lines.getOrElse(cursorRow) { "" }
        val wrappedSegments = TerminalUtils.wrapText(currentLine, width)

        if (cursorCol < currentLine.length) {
            var charsInSegments = 0
            var currentSegment = 0
            for ((idx, segment) in wrappedSegments.withIndex()) {
                if (cursorCol < charsInSegments + segment.length) {
                    currentSegment = idx
                    break
                }
                charsInSegments += segment.length
            }

            val segmentEnd = charsInSegments + wrappedSegments[currentSegment].length

            if (cursorCol == segmentEnd - 1 && currentSegment < wrappedSegments.size - 1) {
                cursorCol++
            } else {
                cursorCol++
            }
        } else if (cursorRow < lines.size - 1) {
            // Move to beginning of next logical line
            cursorRow++
            cursorCol = 0
        }

        autoScroll(visibleHeight)
    }

    fun moveCursorHome() {
        cursorCol = 0
    }

    fun moveCursorEnd() {
        cursorCol = lines.getOrElse(cursorRow) { "" }.length
    }

    // ===== Text Editing =====

    fun insertCharacter(char: Char, visibleHeight: Int) {
        if (cursorRow >= lines.size) {
            lines.add("")
        }
        val line = lines[cursorRow]
        val newLine = line.substring(0, cursorCol.coerceAtMost(line.length)) +
                char + line.substring(cursorCol.coerceAtMost(line.length))
        lines[cursorRow] = newLine
        cursorCol++
        autoScroll(visibleHeight)
    }

    fun insertNewLine(visibleHeight: Int) {
        if (cursorRow < lines.size) {
            val currentLine = lines[cursorRow]
            val beforeCursor = currentLine.substring(0, cursorCol.coerceAtMost(currentLine.length))
            val afterCursor = currentLine.substring(cursorCol.coerceAtMost(currentLine.length))

            lines[cursorRow] = beforeCursor
            lines.add(cursorRow + 1, afterCursor)
            cursorRow++
            cursorCol = 0
        } else {
            lines.add("")
            cursorRow = lines.size - 1
            cursorCol = 0
        }
        autoScroll(visibleHeight)
    }

    fun backspace(visibleHeight: Int): Boolean {
        if (cursorCol > 0 && cursorRow < lines.size) {
            val line = lines[cursorRow]
            lines[cursorRow] = line.substring(0, cursorCol - 1) +
                    line.substring(cursorCol.coerceAtMost(line.length))
            cursorCol--
            autoScroll(visibleHeight)
            return true
        } else if (cursorCol == 0 && cursorRow > 0) {
            // Join with previous line
            val currentLine = lines.getOrElse(cursorRow) { "" }
            cursorRow--
            val prevLine = lines[cursorRow]
            cursorCol = prevLine.length
            lines[cursorRow] = prevLine + currentLine
            if (cursorRow + 1 < lines.size) {
                lines.removeAt(cursorRow + 1)
            }
            autoScroll(visibleHeight)
            return true
        }
        return false
    }
}
