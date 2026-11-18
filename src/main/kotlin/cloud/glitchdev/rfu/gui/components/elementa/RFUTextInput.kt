package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.feature.other.EmojiFeature
import cloud.glitchdev.rfu.gui.components.textinput.ColorCodeText
import gg.essential.elementa.VanillaFontRenderer
import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.elementa.dsl.width
import gg.essential.elementa.font.extractMcScale
import gg.essential.elementa.renderer.ElementaExtractor
import gg.essential.elementa.renderer.fillMcScale
import gg.essential.universal.UKeyboard
import java.awt.Color

/** Keeps editable indices separate from color previews and at the boundaries of rendered emoji aliases. */
abstract class RFUTextInput(
    placeholder: String,
    shadow: Boolean,
    selectionBackgroundColor: Color,
    selectionForegroundColor: Color,
    allowInactiveSelection: Boolean,
    inactiveSelectionBackgroundColor: Color,
    inactiveSelectionForegroundColor: Color,
    cursorColor: Color,
    val allowColors: Boolean = false,
) : AbstractTextInput(
    placeholder, shadow, selectionBackgroundColor, selectionForegroundColor, allowInactiveSelection,
    inactiveSelectionBackgroundColor, inactiveSelectionForegroundColor, cursorColor,
) {
    private var colorPreview = ColorCodeText("")

    private fun preview(): ColorCodeText {
        val text = getText()
        if (colorPreview.text != text) colorPreview = ColorCodeText(text)
        return colorPreview
    }

    private fun lineStart(row: Int): Int {
        val pos = LinePosition(row, 0, isVisual = true).toTextualPos()
        return textualLines.take(pos.line).sumOf { it.length + 1 } + pos.column
    }

    protected fun coloredWidth(row: Int, column: Int = visualLines[row].length): Float {
        val start = lineStart(row)
        return preview().render(start, start + column).width(getTextScale(), getFontProvider())
    }

    protected fun inputCursorScreenPos(): Pair<Float, Float> {
        if (!allowColors) return cursor.toScreenPos()
        val pos = cursor.toVisualPos()
        return coloredWidth(pos.line, pos.column) - horizontalScrollingOffset to
            (lineHeight * pos.line * getTextScale() + verticalScrollingOffset)
    }

    protected fun coloredColumnAtX(row: Int, x: Float): Int {
        val text = visualLines[row].text
        var previousWidth = 0f
        var column = 0
        val emojis = if (getFontProvider() is VanillaFontRenderer) EmojiFeature.findEmojiMatches(text) else emptyList()
        while (column < text.length) {
            val next = emojis.firstOrNull { it.start == column }?.end
                ?: (column + Character.charCount(text.codePointAt(column)))
            val width = coloredWidth(row, next)
            if (x < (previousWidth + width) / 2f) return column
            previousWidth = width
            column = next
        }
        return text.length
    }

    /** Draw the selection behind the preview so formatting remains visible while selecting it. */
    protected fun extractColoredLine(extractor: ElementaExtractor, row: Int, left: Float, y: Float) {
        val scale = getTextScale()
        val (start, end) = getSelection()
        if (hasSelection() && row in start.line..end.line) {
            val from = if (row == start.line) start.column else 0
            val to = if (row == end.line) end.column else visualLines[row].length
            val newlineWidth = if (row < end.line) " ".width(scale, getFontProvider()) else 0f
            extractor.fillMcScale(
                left + coloredWidth(row, from), y,
                left + coloredWidth(row, to) + newlineWidth, y + lineHeight * scale,
                if (active) selectionBackgroundColor else inactiveSelectionBackgroundColor,
            )
        }
        val offset = lineStart(row)
        getFontProvider().extractMcScale(
            extractor, preview().render(offset, offset + visualLines[row].length), getColor(), left, y, scale, shadow,
        )
    }

    /** Preserve raw indices and spaces while measuring the styled preview, including bold and emojis. */
    protected fun splitColoredLine(text: String, offset: Int, maxWidth: Float): List<String> {
        if (maxWidth <= 0f) return listOf(text)
        val result = mutableListOf<String>()
        val emojis = if (getFontProvider() is VanillaFontRenderer) EmojiFeature.findEmojiMatches(text) else emptyList()
        var start = 0
        while (start < text.length) {
            var end = start
            var space = -1
            while (end < text.length) {
                val next = emojis.firstOrNull { it.start == end }?.end
                    ?: (end + Character.charCount(text.codePointAt(end)))
                val width = preview().render(offset + start, offset + next).width(getTextScale(), getFontProvider())
                if (width > maxWidth && end > start) break
                if (text[end] == ' ') space = next
                end = next
                if (width > maxWidth) break
            }
            if (end < text.length && space > start) end = space
            result.add(text.substring(start, end))
            start = end
        }
        return result.ifEmpty { listOf("") }
    }

    init {
        onMouseClick { snapSelectionToEmojis() }
        onMouseDrag { _, _, _ -> snapSelectionToEmojis() }
    }

    protected fun emojiColumnAtX(text: String, x: Float): Int? {
        if (getFontProvider() !is VanillaFontRenderer || getTextScale() <= 0f || !EmojiFeature.hasEmojis(text)) return null
        return EmojiFeature.getClickedRawPosition(mc.font, text, x / getTextScale())
    }

    private fun snapPosition(pos: LinePosition, preferEnd: Boolean): LinePosition {
        val visual = pos.toVisualPos()
        val column = EmojiFeature.snapToEmojiBoundary(visualLines[visual.line].text, visual.column, preferEnd)
        return visual.withColumn(column)
    }

    /** Expand word selections too: Elementa considers the colons to be word boundaries. */
    protected fun snapSelectionToEmojis() {
        if (getFontProvider() !is VanillaFontRenderer) return
        if (hasSelection()) {
            val cursorIsStart = cursor < otherSelectionEnd
            val start = snapPosition(selectionStart(), preferEnd = false)
            val end = snapPosition(selectionEnd(), preferEnd = true)
            cursor = if (cursorIsStart) start else end
            otherSelectionEnd = if (cursorIsStart) end else start
        } else {
            cursor = snapPosition(cursor, preferEnd = true)
            otherSelectionEnd = cursor
        }
    }

    override fun keyType(typedChar: Char, keyCode: Int) {
        if (allowColors && active && (keyCode == UKeyboard.KEY_UP || keyCode == UKeyboard.KEY_DOWN)) {
            val pos = cursor.toVisualPos()
            val row = pos.line + if (keyCode == UKeyboard.KEY_UP) -1 else 1
            cursor = when {
                row < 0 -> LinePosition(0, 0, isVisual = true)
                row > visualLines.lastIndex -> LinePosition(visualLines.lastIndex, visualLines.last().length, isVisual = true)
                else -> LinePosition(row, coloredColumnAtX(row, coloredWidth(pos.line, pos.column)), isVisual = true)
            }
            if (!UKeyboard.isShiftKeyDown()) otherSelectionEnd = cursor
            snapSelectionToEmojis()
            cursorNeedsRefocus = true
            return
        }
        if (!active || getFontProvider() !is VanillaFontRenderer) {
            super.keyType(typedChar, keyCode)
            return
        }

        snapSelectionToEmojis()
        val previousCursor = cursor
        if (!hasSelection() && (keyCode == UKeyboard.KEY_BACKSPACE || keyCode == UKeyboard.KEY_DELETE)) {
            val backwards = keyCode == UKeyboard.KEY_BACKSPACE
            val target = if (UKeyboard.isCtrlKeyDown()) {
                getNearestWordBoundary(cursor, if (backwards) Direction.Left else Direction.Right)
            } else {
                cursor.offsetColumn(if (backwards) -1 else 1)
            }
            val boundary = snapPosition(target, preferEnd = !backwards)
            if (boundary.compareTo(target) != 0) {
                commitTextRemoval(
                    if (backwards) boundary else cursor, if (backwards) cursor else boundary,
                    selectAfterUndo = false,
                )
                return
            }
        }

        super.keyType(typedChar, keyCode)
        val collapsed = !hasSelection()
        val preferEnd = when (keyCode) {
            UKeyboard.KEY_LEFT -> false
            UKeyboard.KEY_RIGHT -> true
            else -> cursor >= previousCursor
        }
        cursor = snapPosition(cursor, preferEnd)
        otherSelectionEnd = if (collapsed) cursor else snapPosition(otherSelectionEnd, otherSelectionEnd >= cursor)
    }
}
