package org.example

/**
 * Manages text selection state and operations
 */
class TextSelection {
    private var startRow: Int? = null
    private var startCol: Int? = null
    private var endRow: Int? = null
    private var endCol: Int? = null

    val isActive: Boolean
        get() = startRow != null

    fun clear() {
        startRow = null
        startCol = null
        endRow = null
        endCol = null
    }

    fun start(row: Int, col: Int) {
        startRow = row
        startCol = col
        endRow = row
        endCol = col
    }

    fun extend(row: Int, col: Int) {
        if (startRow == null) {
            start(row, col)
        } else {
            endRow = row
            endCol = col
        }
    }

    fun selectAll(maxRow: Int, maxCol: Int) {
        startRow = 0
        startCol = 0
        endRow = maxRow
        endCol = maxCol
    }

    /**
     * Check if a character at the given position is selected
     */
    fun isCharSelected(lineIndex: Int, charIndex: Int): Boolean {
        val sRow = startRow ?: return false
        val sCol = startCol ?: return false
        val eRow = endRow ?: return false
        val eCol = endCol ?: return false

        // Normalize selection (start should be before end)
        val (normStartRow, normStartCol, normEndRow, normEndCol) = if (sRow < eRow || (sRow == eRow && sCol <= eCol)) {
            listOf(sRow, sCol, eRow, eCol)
        } else {
            listOf(eRow, eCol, sRow, sCol)
        }

        return when {
            lineIndex < normStartRow || lineIndex > normEndRow -> false
            lineIndex == normStartRow && lineIndex == normEndRow -> charIndex >= normStartCol && charIndex < normEndCol
            lineIndex == normStartRow -> charIndex >= normStartCol
            lineIndex == normEndRow -> charIndex < normEndCol
            else -> true
        }
    }

    /**
     * Delete selected text from the given lines
     * Returns new cursor position (row, col)
     */
    fun deleteFromLines(lines: MutableList<String>): Pair<Int, Int> {
        val sRow = startRow ?: return Pair(0, 0)
        val sCol = startCol ?: return Pair(0, 0)
        val eRow = endRow ?: return Pair(0, 0)
        val eCol = endCol ?: return Pair(0, 0)

        // Normalize selection
        val (normStartRow, normStartCol, normEndRow, normEndCol) = if (sRow < eRow || (sRow == eRow && sCol <= eCol)) {
            listOf(sRow, sCol, eRow, eCol)
        } else {
            listOf(eRow, eCol, sRow, sCol)
        }

        if (normStartRow == normEndRow) {
            // Selection within single line
            val line = lines[normStartRow]
            lines[normStartRow] = line.substring(0, normStartCol) + line.substring(normEndCol)
            clear()
            return Pair(normStartRow, normStartCol)
        } else {
            // Multi-line selection
            val firstLine = lines[normStartRow].substring(0, normStartCol)
            val lastLine = if (normEndRow < lines.size) {
                lines[normEndRow].substring(normEndCol)
            } else {
                ""
            }

            // Remove lines in between
            for (i in normEndRow downTo normStartRow + 1) {
                if (i < lines.size) {
                    lines.removeAt(i)
                }
            }

            // Merge first and last
            lines[normStartRow] = firstLine + lastLine
            clear()
            return Pair(normStartRow, normStartCol)
        }
    }
}
