package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.feature.other.EmojiFeature
import gg.essential.elementa.VanillaFontRenderer
import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.universal.UKeyboard
import java.awt.Color

/** Keeps Elementa's editable text indices at the boundaries of rendered emoji aliases. */
abstract class RFUTextInput(
    placeholder: String,
    shadow: Boolean,
    selectionBackgroundColor: Color,
    selectionForegroundColor: Color,
    allowInactiveSelection: Boolean,
    inactiveSelectionBackgroundColor: Color,
    inactiveSelectionForegroundColor: Color,
    cursorColor: Color,
) : AbstractTextInput(
    placeholder, shadow, selectionBackgroundColor, selectionForegroundColor, allowInactiveSelection,
    inactiveSelectionBackgroundColor, inactiveSelectionForegroundColor, cursorColor,
) {
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
