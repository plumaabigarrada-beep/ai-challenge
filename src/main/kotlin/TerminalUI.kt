package org.example

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

class TerminalUI {
    // State
    private val messages = mutableListOf<ChatMessage>()
    private var chatScrollOffset = 0
    private var terminalHeight = 24
    private var terminalWidth = 80
    private var isRunning = true
    private var isProcessing = false

    // Editor components
    private val promptEditor = PromptEditor { getRightPanelWidth() - 4 }
    private val inputEditor = PromptEditor { terminalWidth - 4 }
    private var temperature = 0.7

    // Panel focus
    private enum class FocusedPanel { CHAT, PROMPT, INPUT, TEMPERATURE }
    private var focusedPanel = FocusedPanel.INPUT

    // Input area scrolling
    private var inputScrollOffset = 0

    data class ChatMessage(val role: String, val content: String, var lineCount: Int = 0)

    init {
        updateTerminalSize()
        promptEditor.loadText(SYSTEM_PROMPT)
    }

    // ===== Terminal Size =====

    private fun updateTerminalSize(): Boolean {
        val (newHeight, newWidth) = TerminalUtils.getTerminalSize(terminalHeight, terminalWidth)
        val changed = newHeight != terminalHeight || newWidth != terminalWidth
        if (changed) {
            terminalHeight = newHeight
            terminalWidth = newWidth
        }
        return changed
    }

    private fun getLeftPanelWidth() = (terminalWidth * 0.55).toInt()
    private fun getRightPanelWidth() = terminalWidth - getLeftPanelWidth() - 1

    // Input area dimensions
    private fun getInputAreaHeight(): Int {
        val contentWidth = terminalWidth - 4
        var totalWrappedLines = 0

        for (line in inputEditor.getLines()) {
            val wrappedLines = TerminalUtils.wrapText(line, contentWidth)
            totalWrappedLines += wrappedLines.size.coerceAtLeast(1)
        }

        if (totalWrappedLines == 0) totalWrappedLines = 1

        val visibleLines = totalWrappedLines.coerceAtMost(80)
        return visibleLines + 2 // +2 for borders
    }

    private fun getInputStartRow(): Int = terminalHeight - getInputAreaHeight() + 1
    private fun getInputEndRow(): Int = terminalHeight
    private fun getStatusBarRow(): Int = getInputStartRow() - 1
    private fun getChatPromptEndRow(): Int = getStatusBarRow() - 1

    // ===== Chat Management =====

    private fun calculateChatLines(): Int {
        val contentWidth = getLeftPanelWidth() - 4
        var total = 0
        for (message in messages) {
            total += 1 // Role header
            val wrappedContent = TerminalUtils.wrapText(TerminalUtils.cleanMarkdown(message.content), contentWidth)
            total += wrappedContent.size + 1 // Content + empty line
            message.lineCount = wrappedContent.size + 2
        }
        return total
    }

    fun addMessage(role: String, content: String) {
        messages.add(ChatMessage(role, content))
        scrollChatToBottom()
        render()
    }

    private fun scrollChatToBottom() {
        val chatAreaHeight = getChatPromptEndRow() - 3
        val totalLines = calculateChatLines()
        chatScrollOffset = (totalLines - chatAreaHeight).coerceAtLeast(0)
    }

    private fun scrollChatUp(lines: Int = 3) {
        chatScrollOffset = (chatScrollOffset - lines).coerceAtLeast(0)
        render()
    }

    private fun scrollChatDown(lines: Int = 3) {
        val totalLines = calculateChatLines()
        val chatAreaHeight = getChatPromptEndRow() - 3
        val maxOffset = (totalLines - chatAreaHeight).coerceAtLeast(0)
        chatScrollOffset = (chatScrollOffset + lines).coerceAtMost(maxOffset)
        render()
    }

    // ===== Drawing =====

    private fun drawBorder(startRow: Int, endRow: Int, startCol: Int, endCol: Int, title: String = "", focused: Boolean = false) {
        val width = endCol - startCol + 1
        val titleColor = if (focused) AnsiCodes.CYAN else AnsiCodes.BLUE

        val topBorder = if (title.isEmpty()) {
            "╔" + "═".repeat(width - 2) + "╗"
        } else {
            val titleText = if (focused) " $title ★ " else " $title "
            val leftPadding = ((width - 2 - titleText.length) / 2).coerceAtLeast(0)
            val rightPadding = (width - 2 - titleText.length - leftPadding).coerceAtLeast(0)
            "╔" + "═".repeat(leftPadding) + titleText + "═".repeat(rightPadding) + "╗"
        }

        val bottomBorder = "╚" + "═".repeat(width - 2) + "╝"
        val emptyLine = "║" + " ".repeat(width - 2) + "║"

        print(AnsiCodes.moveCursor(startRow, startCol) + titleColor + topBorder + AnsiCodes.RESET)
        for (i in (startRow + 1) until endRow) {
            print(AnsiCodes.moveCursor(i, startCol) + titleColor + emptyLine + AnsiCodes.RESET)
        }
        print(AnsiCodes.moveCursor(endRow, startCol) + titleColor + bottomBorder + AnsiCodes.RESET)
    }

    private fun drawVerticalDivider() {
        val dividerCol = getLeftPanelWidth() + 1
        for (row in 3..getChatPromptEndRow()) {
            print(AnsiCodes.moveCursor(row, dividerCol) + AnsiCodes.BLUE + "│" + AnsiCodes.RESET)
        }
    }

    private fun drawHeader() {
        val title = "🎲 D&D CHARACTER CREATOR - PERPLEXITY AI CHAT 🎲"
        val titlePadding = ((terminalWidth - title.length) / 2).coerceAtLeast(0)
        print(AnsiCodes.moveCursor(1, 1) + AnsiCodes.CLEAR_LINE)
        print(AnsiCodes.moveCursor(1, titlePadding) + AnsiCodes.CYAN + AnsiCodes.BOLD + title + AnsiCodes.RESET)
    }

    private fun drawChatPanel() {
        val chatAreaStart = 3
        val chatAreaEnd = getChatPromptEndRow()
        val leftStart = 3
        val contentWidth = getLeftPanelWidth() - 4
        val visibleHeight = chatAreaEnd - chatAreaStart - 1

        var currentRow = chatAreaStart + 1
        var linesSkipped = 0
        var messageStartIndex = 0

        // Find which message to start from
        for ((index, message) in messages.withIndex()) {
            if (linesSkipped + message.lineCount <= chatScrollOffset) {
                linesSkipped += message.lineCount
                messageStartIndex = index + 1
            } else {
                break
            }
        }

        val linesToSkipInFirstMessage = chatScrollOffset - linesSkipped

        for ((index, message) in messages.withIndex()) {
            if (index < messageStartIndex) continue
            if (currentRow >= chatAreaEnd) break

            val isFirstMessage = (index == messageStartIndex)
            var lineOffset = if (isFirstMessage) linesToSkipInFirstMessage else 0

            // Draw role header
            if (lineOffset == 0) {
                val roleHeader = when (message.role) {
                    "assistant" -> "${AnsiCodes.CYAN}${AnsiCodes.BOLD}🎲 AI Master${AnsiCodes.RESET}"
                    "user" -> "${AnsiCodes.GREEN}${AnsiCodes.BOLD}👤 You${AnsiCodes.RESET}"
                    "system" -> "${AnsiCodes.YELLOW}${AnsiCodes.BOLD}⚙ System${AnsiCodes.RESET}"
                    else -> message.role
                }
                print(AnsiCodes.moveCursor(currentRow, leftStart) + AnsiCodes.CLEAR_LINE + roleHeader)
                currentRow++
                if (currentRow >= chatAreaEnd) break
            } else {
                lineOffset--
            }

            // Draw message content
            val wrappedContent = TerminalUtils.wrapText(TerminalUtils.cleanMarkdown(message.content), contentWidth)
            for (line in wrappedContent) {
                if (lineOffset > 0) {
                    lineOffset--
                    continue
                }
                if (currentRow >= chatAreaEnd) break
                val truncated = line.take(contentWidth)
                print(AnsiCodes.moveCursor(currentRow, leftStart) + AnsiCodes.WHITE + truncated +
                      " ".repeat((contentWidth - truncated.length).coerceAtLeast(0)) + AnsiCodes.RESET)
                currentRow++
            }

            // Empty line between messages
            if (lineOffset == 0 && currentRow < chatAreaEnd) {
                currentRow++
            }
        }

        // Clear remaining lines
        while (currentRow < chatAreaEnd) {
            print(AnsiCodes.moveCursor(currentRow, leftStart) + " ".repeat(contentWidth))
            currentRow++
        }

        // Draw scroll indicator if needed
        val totalLines = calculateChatLines()
        val maxOffset = (totalLines - visibleHeight).coerceAtLeast(0)
        if (maxOffset > 0) {
            drawScrollIndicator(chatAreaStart + 1, chatAreaEnd - 1, getLeftPanelWidth() - 1, chatScrollOffset, maxOffset)
        }
    }

    private fun drawTemperatureField() {
        val tempRow = 3
        val rightStart = getLeftPanelWidth() + 3
        val label = "Temperature: "
        val tempStr = String.format("%.1f", temperature)
        val focused = focusedPanel == FocusedPanel.TEMPERATURE
        val color = if (focused) "${AnsiCodes.YELLOW}${AnsiCodes.BOLD}" else AnsiCodes.WHITE

        print(AnsiCodes.moveCursor(tempRow, rightStart) + AnsiCodes.GRAY + label + color + tempStr + AnsiCodes.RESET)
    }

    private fun drawPromptPanel() {
        val promptAreaStart = 4
        val promptAreaEnd = getChatPromptEndRow()
        val rightStart = getLeftPanelWidth() + 3
        val contentWidth = getRightPanelWidth() - 4
        val visibleHeight = promptAreaEnd - promptAreaStart - 1

        // Update scroll constraints
        promptEditor.updateScrollConstraints(visibleHeight)

        var currentRow = promptAreaStart + 1
        var wrappedLineCount = 0

        for (lineIndex in promptEditor.getLines().indices) {
            if (currentRow >= promptAreaEnd) break

            val line = promptEditor.getLines()[lineIndex]
            val wrappedLines = TerminalUtils.wrapText(line, contentWidth)

            // Skip lines before scroll offset
            if (wrappedLineCount + wrappedLines.size <= promptEditor.scrollOffset) {
                wrappedLineCount += wrappedLines.size
                continue
            }

            val startSegment = if (wrappedLineCount < promptEditor.scrollOffset) {
                promptEditor.scrollOffset - wrappedLineCount
            } else {
                0
            }

            for (segmentIndex in startSegment until wrappedLines.size) {
                if (currentRow >= promptAreaEnd) break

                val segment = wrappedLines[segmentIndex]
                val isCursorLine = focusedPanel == FocusedPanel.PROMPT && lineIndex == promptEditor.cursorRow
                val charOffset = wrappedLines.take(segmentIndex).sumOf { it.length }

                // Build display line with selection highlighting
                val displayLine = StringBuilder()
                for (i in segment.indices) {
                    val charPos = charOffset + i
                    val char = segment[i]

                    if (promptEditor.selection.isCharSelected(lineIndex, charPos)) {
                        displayLine.append("${AnsiCodes.RESET}${AnsiCodes.REVERSE_VIDEO}$char${AnsiCodes.RESET}")
                    } else {
                        displayLine.append(char)
                    }
                }

                val displayStr = displayLine.toString()
                val visibleLen = segment.length
                val padding = " ".repeat((contentWidth - visibleLen).coerceAtLeast(0))

                val color = if (isCursorLine) "${AnsiCodes.YELLOW}${AnsiCodes.BOLD}" else AnsiCodes.WHITE

                print(AnsiCodes.moveCursor(currentRow, rightStart) + color + displayStr + padding + AnsiCodes.RESET)
                currentRow++
            }

            wrappedLineCount += wrappedLines.size
        }

        // Clear remaining lines
        while (currentRow < promptAreaEnd) {
            print(AnsiCodes.moveCursor(currentRow, rightStart) + " ".repeat(contentWidth))
            currentRow++
        }

        // Draw scroll indicator
        val totalWrapped = promptEditor.calculateTotalWrappedLines()
        val maxOffset = (totalWrapped - visibleHeight).coerceAtLeast(0)
        if (maxOffset > 0) {
            drawScrollIndicator(promptAreaStart + 1, promptAreaEnd - 1, terminalWidth - 1, promptEditor.scrollOffset, maxOffset)
        }
    }

    private fun drawScrollIndicator(startRow: Int, endRow: Int, col: Int, offset: Int, maxOffset: Int) {
        if (maxOffset == 0) return

        val scrollBarHeight = endRow - startRow + 1
        val scrollPercentage = offset.toFloat() / maxOffset
        val thumbPosition = startRow + (scrollPercentage * (scrollBarHeight - 1)).toInt()

        for (row in startRow..endRow) {
            val char = if (row == thumbPosition) "█" else "│"
            val color = if (row == thumbPosition) AnsiCodes.CYAN else AnsiCodes.GRAY
            print(AnsiCodes.moveCursor(row, col) + color + char + AnsiCodes.RESET)
        }
    }

    private fun drawStatusBar() {
        val statusRow = getStatusBarRow()

        val panelInfo = when (focusedPanel) {
            FocusedPanel.CHAT -> "${AnsiCodes.GREEN}[Chat]${AnsiCodes.RESET} ${AnsiCodes.GRAY}Temp Prompt Input${AnsiCodes.RESET}"
            FocusedPanel.TEMPERATURE -> "${AnsiCodes.GRAY}Chat${AnsiCodes.RESET} ${AnsiCodes.GREEN}[Temperature]${AnsiCodes.RESET} ${AnsiCodes.GRAY}Prompt Input${AnsiCodes.RESET}"
            FocusedPanel.PROMPT -> "${AnsiCodes.GRAY}Chat Temp${AnsiCodes.RESET} ${AnsiCodes.GREEN}[System Prompt]${AnsiCodes.RESET} ${AnsiCodes.GRAY}Input${AnsiCodes.RESET}"
            FocusedPanel.INPUT -> "${AnsiCodes.GRAY}Chat Temp Prompt${AnsiCodes.RESET} ${AnsiCodes.GREEN}[Input]${AnsiCodes.RESET}"
        }

        val selectionInfo = if (focusedPanel == FocusedPanel.PROMPT && promptEditor.selection.isActive) {
            " | ${AnsiCodes.CYAN}Selection Active${AnsiCodes.RESET}"
        } else {
            ""
        }

        val scrollInfo = when (focusedPanel) {
            FocusedPanel.PROMPT -> " | ↑↓←→: Move | Ctrl+A: Select All | Shift+Arrow: Select | Ln ${promptEditor.cursorRow + 1}/${promptEditor.getLines().size}$selectionInfo"
            FocusedPanel.TEMPERATURE -> " | ↑↓: Adjust (${String.format("%.1f", temperature)})"
            FocusedPanel.INPUT -> " | Shift/Ctrl+Enter: New line | Enter: Send | Ln ${inputEditor.cursorRow + 1}/${inputEditor.getLines().size}"
            else -> ""
        }

        val status = if (isProcessing) {
            "${AnsiCodes.YELLOW}⏳ AI is thinking...${AnsiCodes.RESET}"
        } else {
            "$panelInfo$scrollInfo | Tab: Switch | 'quit' to exit"
        }

        print(AnsiCodes.moveCursor(statusRow, 1) + AnsiCodes.CLEAR_LINE)
        print(AnsiCodes.moveCursor(statusRow, 3) + AnsiCodes.GRAY + status.take(terminalWidth - 6) + AnsiCodes.RESET)
    }

    private fun drawInputArea() {
        val inputAreaStart = getInputStartRow()
        val inputAreaEnd = getInputEndRow()
        val contentStart = 3
        val contentWidth = terminalWidth - 4
        val visibleHeight = inputAreaEnd - inputAreaStart - 1

        // Update scroll constraints
        inputEditor.updateScrollConstraints(visibleHeight)

        var currentRow = inputAreaStart + 1
        var wrappedLineCount = 0

        for (lineIndex in inputEditor.getLines().indices) {
            if (currentRow >= inputAreaEnd) break

            val line = inputEditor.getLines()[lineIndex]
            val wrappedLines = TerminalUtils.wrapText(line, contentWidth)

            // Skip lines before scroll offset
            if (wrappedLineCount + wrappedLines.size <= inputScrollOffset) {
                wrappedLineCount += wrappedLines.size
                continue
            }

            val startSegment = if (wrappedLineCount < inputScrollOffset) {
                inputScrollOffset - wrappedLineCount
            } else {
                0
            }

            for (segmentIndex in startSegment until wrappedLines.size) {
                if (currentRow >= inputAreaEnd) break

                val segment = wrappedLines[segmentIndex]
                val isCursorLine = focusedPanel == FocusedPanel.INPUT && lineIndex == inputEditor.cursorRow

                val displayStr = segment
                val visibleLen = segment.length
                val padding = " ".repeat((contentWidth - visibleLen).coerceAtLeast(0))

                val color = if (isCursorLine) "${AnsiCodes.GREEN}${AnsiCodes.BOLD}" else AnsiCodes.WHITE

                print(AnsiCodes.moveCursor(currentRow, contentStart) + color + displayStr + padding + AnsiCodes.RESET)
                currentRow++
            }

            wrappedLineCount += wrappedLines.size
        }

        // Clear remaining lines
        while (currentRow < inputAreaEnd) {
            print(AnsiCodes.moveCursor(currentRow, contentStart) + " ".repeat(contentWidth))
            currentRow++
        }

        // Draw scroll indicator if needed
        val totalWrapped = inputEditor.calculateTotalWrappedLines()
        val maxOffset = (totalWrapped - visibleHeight).coerceAtLeast(0)
        if (maxOffset > 0) {
            drawScrollIndicator(inputAreaStart + 1, inputAreaEnd - 1, terminalWidth - 1, inputScrollOffset, maxOffset)
        }
    }

    fun render() {
        print(AnsiCodes.HIDE_CURSOR)

        // Draw panels
        drawBorder(2, getChatPromptEndRow(), 1, getLeftPanelWidth(), "Chat", focusedPanel == FocusedPanel.CHAT)
        drawBorder(2, getChatPromptEndRow(), getLeftPanelWidth() + 2, terminalWidth, "System Prompt", focusedPanel == FocusedPanel.PROMPT)
        drawBorder(getInputStartRow(), getInputEndRow(), 1, terminalWidth, "Input", focusedPanel == FocusedPanel.INPUT)

        drawVerticalDivider()
        drawHeader()
        drawChatPanel()
        drawTemperatureField()
        drawPromptPanel()
        drawStatusBar()
        drawInputArea()

        print(AnsiCodes.SHOW_CURSOR)

        // Position cursor
        when (focusedPanel) {
            FocusedPanel.INPUT -> {
                val inputAreaStart = getInputStartRow()
                val contentStart = 3
                val contentWidth = terminalWidth - 4
                val visibleHeight = getInputEndRow() - inputAreaStart - 1

                val displayRow = inputEditor.getDisplayRowForCursor()
                val visibleRow = displayRow - inputScrollOffset

                if (visibleRow in 0 until visibleHeight) {
                    val currentLine = inputEditor.getLines().getOrElse(inputEditor.cursorRow) { "" }
                    val wrappedSegments = TerminalUtils.wrapText(currentLine, contentWidth)

                    var charsProcessed = 0
                    var colInSegment = inputEditor.cursorCol
                    for ((idx, segment) in wrappedSegments.withIndex()) {
                        if (inputEditor.cursorCol <= charsProcessed + segment.length) {
                            colInSegment = inputEditor.cursorCol - charsProcessed
                            break
                        }
                        charsProcessed += segment.length
                    }

                    val row = inputAreaStart + 1 + visibleRow
                    val col = contentStart + colInSegment.coerceIn(0, contentWidth - 1)
                    print(AnsiCodes.moveCursor(row, col))
                }
            }
            FocusedPanel.TEMPERATURE -> {
                val tempRow = 3
                val rightStart = getLeftPanelWidth() + 3
                val label = "Temperature: "
                val tempStr = String.format("%.1f", temperature)
                print(AnsiCodes.moveCursor(tempRow, rightStart + label.length + tempStr.length))
            }
            FocusedPanel.PROMPT -> {
                val promptAreaStart = 4
                val rightStart = getLeftPanelWidth() + 3
                val contentWidth = getRightPanelWidth() - 4
                val visibleHeight = getChatPromptEndRow() - promptAreaStart - 1

                val displayRow = promptEditor.getDisplayRowForCursor()
                val visibleRow = displayRow - promptEditor.scrollOffset

                if (visibleRow in 0 until visibleHeight) {
                    val currentLine = promptEditor.getLines().getOrElse(promptEditor.cursorRow) { "" }
                    val wrappedSegments = TerminalUtils.wrapText(currentLine, contentWidth)

                    var charsProcessed = 0
                    var colInSegment = promptEditor.cursorCol
                    for ((idx, segment) in wrappedSegments.withIndex()) {
                        if (promptEditor.cursorCol <= charsProcessed + segment.length) {
                            colInSegment = promptEditor.cursorCol - charsProcessed
                            break
                        }
                        charsProcessed += segment.length
                    }

                    val row = promptAreaStart + 1 + visibleRow
                    val col = rightStart + colInSegment.coerceIn(0, contentWidth - 1)
                    print(AnsiCodes.moveCursor(row, col))
                }
            }
            else -> {}
        }

        System.out.flush()
    }

    // ===== Conversation Management =====

    private fun updateSystemMessage(conversationHistory: MutableList<Message>) {
        val systemPromptText = promptEditor.getText().trim()
        val systemMessageIndex = conversationHistory.indexOfFirst { it.role == "system" }

        if (systemPromptText.isEmpty()) {
            if (systemMessageIndex >= 0) {
                conversationHistory.removeAt(systemMessageIndex)
            }
        } else {
            if (systemMessageIndex >= 0) {
                conversationHistory[systemMessageIndex] = Message(role = "system", content = systemPromptText)
            } else {
                conversationHistory.add(0, Message(role = "system", content = systemPromptText))
            }
        }
    }

    // ===== Main Loop =====

    suspend fun start() = coroutineScope {
        print(AnsiCodes.CLEAR_SCREEN)
        print(AnsiCodes.HIDE_CURSOR)

        val client = PerplexityClient()
        val conversationHistory = mutableListOf<Message>()
        updateSystemMessage(conversationHistory)

        render()

        // Terminal size monitor
        launch {
            while (isRunning) {
                delay(300)
                if (updateTerminalSize()) {
                    print(AnsiCodes.CLEAR_SCREEN)
                    render()
                    delay(200)
                }
            }
        }

        // Input handling
        val inputJob = launch(Dispatchers.IO) {
            handleInput(client, conversationHistory)
        }

        inputJob.join()

        print(AnsiCodes.CLEAR_SCREEN)
        print(AnsiCodes.SHOW_CURSOR)
        print(AnsiCodes.moveCursor(1, 1))
        println("\n${AnsiCodes.CYAN}Thank you for using D&D Character Creator! 🎲${AnsiCodes.RESET}\n")

        client.close()
    }

    private suspend fun handleInput(client: PerplexityClient, conversationHistory: MutableList<Message>) {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        val escapeBuffer = mutableListOf<Int>()

        while (isRunning) {
            if (reader.ready()) {
                val char = reader.read()

                // Handle escape sequences
                if (char == 27) {
                    escapeBuffer.clear()
                    escapeBuffer.add(char)
                    delay(10)

                    while (reader.ready() && escapeBuffer.size < 6) {
                        escapeBuffer.add(reader.read())
                    }

                    // Cmd+V paste: ESC v (on macOS in some terminals)
                    // Actually Cmd+V sends paste directly, we'll handle it differently

                    // Shift+Enter or Ctrl+Enter: Various possible sequences
                    // Shift+Enter: ESC [13;2~ or ESC [27;2;13~ or ESC O M
                    // Ctrl+Enter: ESC [13;5~ or ESC [27;5;13~
                    val isShiftOrCtrlEnter =
                        // Shift+Enter patterns
                        (escapeBuffer.size >= 5 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 51 && escapeBuffer[4] == 59 && escapeBuffer.getOrNull(5) == 50) ||
                        (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 50 &&
                        escapeBuffer[3] == 55 && escapeBuffer[4] == 59 && escapeBuffer[5] == 50) ||
                        (escapeBuffer.size >= 3 && escapeBuffer[1] == 79 && escapeBuffer[2] == 77) ||
                        // Ctrl+Enter patterns
                        (escapeBuffer.size >= 5 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 51 && escapeBuffer[4] == 59 && escapeBuffer.getOrNull(5) == 53) ||
                        (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 50 &&
                        escapeBuffer[3] == 55 && escapeBuffer[4] == 59 && escapeBuffer[5] == 53)

                    if (isShiftOrCtrlEnter) {
                        // Shift+Enter or Ctrl+Enter - new line
                        when (focusedPanel) {
                            FocusedPanel.INPUT -> {
                                val oldHeight = getInputAreaHeight()
                                val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                inputEditor.insertNewLine(visibleHeight)
                                val newHeight = getInputAreaHeight()
                                if (oldHeight != newHeight) {
                                    print(AnsiCodes.CLEAR_SCREEN)
                                }
                                render()
                            }
                            FocusedPanel.PROMPT -> {
                                val visibleHeight = getChatPromptEndRow() - 4 - 1
                                promptEditor.insertNewLine(visibleHeight)
                                render()
                            }
                            else -> {}
                        }
                        continue
                    }

                    // Shift+Left: ESC [1;2D
                    if (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 59 && escapeBuffer[4] == 50 && escapeBuffer[5] == 68) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            val visibleHeight = getChatPromptEndRow() - 4 - 1
                            if (!promptEditor.selection.isActive) {
                                promptEditor.selection.start(promptEditor.cursorRow, promptEditor.cursorCol)
                            }
                            promptEditor.moveCursorLeft(visibleHeight)
                            promptEditor.selection.extend(promptEditor.cursorRow, promptEditor.cursorCol)
                            render()
                        } else {
                            focusedPanel = FocusedPanel.CHAT
                            render()
                        }
                        continue
                    }

                    // Shift+Right: ESC [1;2C
                    if (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 59 && escapeBuffer[4] == 50 && escapeBuffer[5] == 67) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            val visibleHeight = getChatPromptEndRow() - 4 - 1
                            if (!promptEditor.selection.isActive) {
                                promptEditor.selection.start(promptEditor.cursorRow, promptEditor.cursorCol)
                            }
                            promptEditor.moveCursorRight(visibleHeight)
                            promptEditor.selection.extend(promptEditor.cursorRow, promptEditor.cursorCol)
                            render()
                        } else {
                            focusedPanel = FocusedPanel.PROMPT
                            render()
                        }
                        continue
                    }

                    // Shift+Up: ESC [1;2A
                    if (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 59 && escapeBuffer[4] == 50 && escapeBuffer[5] == 65) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            val visibleHeight = getChatPromptEndRow() - 4 - 1
                            if (!promptEditor.selection.isActive) {
                                promptEditor.selection.start(promptEditor.cursorRow, promptEditor.cursorCol)
                            }
                            promptEditor.moveCursorUp(visibleHeight)
                            promptEditor.selection.extend(promptEditor.cursorRow, promptEditor.cursorCol)
                            render()
                        }
                        continue
                    }

                    // Shift+Down: ESC [1;2B
                    if (escapeBuffer.size >= 6 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 &&
                        escapeBuffer[3] == 59 && escapeBuffer[4] == 50 && escapeBuffer[5] == 66) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            val visibleHeight = getChatPromptEndRow() - 4 - 1
                            if (!promptEditor.selection.isActive) {
                                promptEditor.selection.start(promptEditor.cursorRow, promptEditor.cursorCol)
                            }
                            promptEditor.moveCursorDown(visibleHeight)
                            promptEditor.selection.extend(promptEditor.cursorRow, promptEditor.cursorCol)
                            render()
                        }
                        continue
                    }

                    // Home: ESC [H or ESC [1~
                    if ((escapeBuffer.size >= 3 && escapeBuffer[1] == 91 && escapeBuffer[2] == 72) ||
                        (escapeBuffer.size >= 4 && escapeBuffer[1] == 91 && escapeBuffer[2] == 49 && escapeBuffer[3] == 126)) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            promptEditor.moveCursorHome()
                            render()
                        }
                        continue
                    }

                    // End: ESC [F or ESC [4~
                    if ((escapeBuffer.size >= 3 && escapeBuffer[1] == 91 && escapeBuffer[2] == 70) ||
                        (escapeBuffer.size >= 4 && escapeBuffer[1] == 91 && escapeBuffer[2] == 52 && escapeBuffer[3] == 126)) {
                        if (focusedPanel == FocusedPanel.PROMPT) {
                            promptEditor.moveCursorEnd()
                            render()
                        }
                        continue
                    }

                    // Regular arrow keys: ESC [ A/B/C/D
                    if (escapeBuffer.size >= 3 && escapeBuffer[1] == 91) {
                        when (escapeBuffer[2]) {
                            65 -> { // Up
                                when (focusedPanel) {
                                    FocusedPanel.CHAT -> scrollChatUp()
                                    FocusedPanel.PROMPT -> {
                                        val visibleHeight = getChatPromptEndRow() - 4 - 1
                                        promptEditor.selection.clear()
                                        promptEditor.moveCursorUp(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.INPUT -> {
                                        val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                        inputEditor.moveCursorUp(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.TEMPERATURE -> {
                                        temperature = (temperature + 0.1).coerceAtMost(2.0)
                                        render()
                                    }
                                    else -> {}
                                }
                            }
                            66 -> { // Down
                                when (focusedPanel) {
                                    FocusedPanel.CHAT -> scrollChatDown()
                                    FocusedPanel.PROMPT -> {
                                        val visibleHeight = getChatPromptEndRow() - 4 - 1
                                        promptEditor.selection.clear()
                                        promptEditor.moveCursorDown(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.INPUT -> {
                                        val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                        inputEditor.moveCursorDown(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.TEMPERATURE -> {
                                        temperature = (temperature - 0.1).coerceAtLeast(0.0)
                                        render()
                                    }
                                    else -> {}
                                }
                            }
                            67 -> { // Right
                                when (focusedPanel) {
                                    FocusedPanel.PROMPT -> {
                                        val visibleHeight = getChatPromptEndRow() - 4 - 1
                                        promptEditor.selection.clear()
                                        promptEditor.moveCursorRight(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.INPUT -> {
                                        val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                        inputEditor.moveCursorRight(visibleHeight)
                                        render()
                                    }
                                    else -> {}
                                }
                            }
                            68 -> { // Left
                                when (focusedPanel) {
                                    FocusedPanel.PROMPT -> {
                                        val visibleHeight = getChatPromptEndRow() - 4 - 1
                                        promptEditor.selection.clear()
                                        promptEditor.moveCursorLeft(visibleHeight)
                                        render()
                                    }
                                    FocusedPanel.INPUT -> {
                                        val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                        inputEditor.moveCursorLeft(visibleHeight)
                                        render()
                                    }
                                    else -> {}
                                }
                            }
                        }
                        continue
                    }
                    continue
                }

                // Handle regular keys
                when (char) {
                    13 -> { // Enter (CR)
                        when (focusedPanel) {
                            FocusedPanel.INPUT -> {
                                // Enter sends the message
                                val messageText = inputEditor.getText().trim()
                                if (messageText.isNotEmpty()) {
                                    inputEditor.loadText("")
                                    print(AnsiCodes.CLEAR_SCREEN)
                                    render()

                                    if (messageText.lowercase() in listOf("quit", "exit", "q")) {
                                        isRunning = false
                                        return
                                    }

                                    addMessage("user", messageText)
                                    conversationHistory.add(Message(role = "user", content = messageText))
                                    updateSystemMessage(conversationHistory)

                                    isProcessing = true
                                    render()

                                    val response = client.sendMessage(conversationHistory, temperature)
                                    isProcessing = false

                                    if (response != null && !response.startsWith("Error:")) {
                                        conversationHistory.add(Message(role = "assistant", content = response))
                                        addMessage("assistant", response)
                                    } else {
                                        addMessage("system", response ?: "Failed to get response. Please try again.")
                                    }
                                }
                            }
                            FocusedPanel.PROMPT -> {
                                val visibleHeight = getChatPromptEndRow() - 4 - 1
                                promptEditor.insertNewLine(visibleHeight)
                                render()
                            }
                            else -> {}
                        }
                    }
                    10 -> { // Enter (LF) - same as CR
                        when (focusedPanel) {
                            FocusedPanel.INPUT -> {
                                // Enter sends the message
                                val messageText = inputEditor.getText().trim()
                                if (messageText.isNotEmpty()) {
                                    inputEditor.loadText("")
                                    print(AnsiCodes.CLEAR_SCREEN)
                                    render()

                                    if (messageText.lowercase() in listOf("quit", "exit", "q")) {
                                        isRunning = false
                                        return
                                    }

                                    addMessage("user", messageText)
                                    conversationHistory.add(Message(role = "user", content = messageText))
                                    updateSystemMessage(conversationHistory)

                                    isProcessing = true
                                    render()

                                    val response = client.sendMessage(conversationHistory, temperature)
                                    isProcessing = false

                                    if (response != null && !response.startsWith("Error:")) {
                                        conversationHistory.add(Message(role = "assistant", content = response))
                                        addMessage("assistant", response)
                                    } else {
                                        addMessage("system", response ?: "Failed to get response. Please try again.")
                                    }
                                }
                            }
                            FocusedPanel.PROMPT -> {
                                val visibleHeight = getChatPromptEndRow() - 4 - 1
                                promptEditor.insertNewLine(visibleHeight)
                                render()
                            }
                            else -> {}
                        }
                    }
                    1 -> { // Ctrl+A
                        if (focusedPanel == FocusedPanel.PROMPT && promptEditor.getLines().isNotEmpty()) {
                            promptEditor.selection.selectAll(
                                promptEditor.getLines().size - 1,
                                promptEditor.getLines().last().length
                            )
                            render()
                        }
                    }
                    9 -> { // Tab
                        focusedPanel = when (focusedPanel) {
                            FocusedPanel.INPUT -> FocusedPanel.CHAT
                            FocusedPanel.CHAT -> FocusedPanel.TEMPERATURE
                            FocusedPanel.TEMPERATURE -> FocusedPanel.PROMPT
                            FocusedPanel.PROMPT -> FocusedPanel.INPUT
                        }
                        render()
                    }
                    127, 8 -> { // Backspace
                        when (focusedPanel) {
                            FocusedPanel.INPUT -> {
                                val oldHeight = getInputAreaHeight()
                                val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                inputEditor.backspace(visibleHeight)
                                val newHeight = getInputAreaHeight()
                                if (oldHeight != newHeight) {
                                    print(AnsiCodes.CLEAR_SCREEN)
                                }
                                render()
                            }
                            FocusedPanel.PROMPT -> {
                                val visibleHeight = getChatPromptEndRow() - 4 - 1
                                if (promptEditor.selection.isActive) {
                                    val (newRow, newCol) = promptEditor.selection.deleteFromLines(promptEditor.getLinesForEditing())
                                    promptEditor.cursorRow = newRow
                                    promptEditor.cursorCol = newCol
                                    render()
                                } else {
                                    promptEditor.backspace(visibleHeight)
                                    render()
                                }
                            }
                            else -> {}
                        }
                    }
                    3 -> { // Ctrl+C
                        isRunning = false
                        break
                    }
                    22 -> { // Ctrl+V - paste
                        try {
                            val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                            val contents = clipboard.getContents(null)
                            if (contents != null && contents.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                                val pastedText = contents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor) as String
                                when (focusedPanel) {
                                    FocusedPanel.INPUT -> {
                                        val oldHeight = getInputAreaHeight()
                                        val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                        // Insert pasted text at cursor position
                                        for (pastedChar in pastedText) {
                                            if (pastedChar == '\n' || pastedChar == '\r') {
                                                inputEditor.insertNewLine(visibleHeight)
                                            } else if (pastedChar.code in 32..126 || pastedChar == '\t') {
                                                inputEditor.insertCharacter(pastedChar, visibleHeight)
                                            }
                                        }
                                        val newHeight = getInputAreaHeight()
                                        if (oldHeight != newHeight) {
                                            print(AnsiCodes.CLEAR_SCREEN)
                                        }
                                        render()
                                    }
                                    FocusedPanel.PROMPT -> {
                                        val visibleHeight = getChatPromptEndRow() - 4 - 1
                                        for (pastedChar in pastedText) {
                                            if (pastedChar == '\n' || pastedChar == '\r') {
                                                promptEditor.insertNewLine(visibleHeight)
                                            } else if (pastedChar.code in 32..126 || pastedChar == '\t') {
                                                promptEditor.insertCharacter(pastedChar, visibleHeight)
                                            }
                                        }
                                        render()
                                    }
                                    else -> {}
                                }
                            }
                        } catch (e: Exception) {
                            // Clipboard access failed, ignore
                        }
                    }
                    else -> {
                        if (char in 32..126) { // Printable characters
                            when (focusedPanel) {
                                FocusedPanel.INPUT -> {
                                    val oldHeight = getInputAreaHeight()
                                    val visibleHeight = getInputEndRow() - getInputStartRow() - 1
                                    inputEditor.insertCharacter(char.toChar(), visibleHeight)
                                    val newHeight = getInputAreaHeight()
                                    if (oldHeight != newHeight) {
                                        print(AnsiCodes.CLEAR_SCREEN)
                                    }
                                    render()
                                }
                                FocusedPanel.PROMPT -> {
                                    val visibleHeight = getChatPromptEndRow() - 4 - 1
                                    if (promptEditor.selection.isActive) {
                                        val (newRow, newCol) = promptEditor.selection.deleteFromLines(promptEditor.getLinesForEditing())
                                        promptEditor.cursorRow = newRow
                                        promptEditor.cursorCol = newCol
                                    }
                                    promptEditor.insertCharacter(char.toChar(), visibleHeight)
                                    render()
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
            delay(10)
        }
    }
}
