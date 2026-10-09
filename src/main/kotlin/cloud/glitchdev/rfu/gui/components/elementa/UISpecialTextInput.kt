/*
 * Copyright (C) 2025 EssentialGG (Elementa)
 * Copyright (C) 2025 Riccio (Modifications)
 *
 * This file is part of Elementa (modified).
 *
 * Elementa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Elementa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.constraints.WidthConstraint
import gg.essential.elementa.dsl.basicYConstraint
import gg.essential.elementa.dsl.coerceIn
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width
import gg.essential.elementa.font.extractMcScale
import gg.essential.elementa.renderer.ElementaExtractor
import java.awt.Color

/**
 * Same as Elementa's original Text Input but modified to have centered text and shadows on user input
 */
open class UISpecialTextInput @JvmOverloads constructor(
    placeholder: String = "",
    shadow: Boolean = true,
    selectionBackgroundColor: Color = Color.WHITE,
    selectionForegroundColor: Color = Color(64, 139, 229),
    allowInactiveSelection: Boolean = false,
    inactiveSelectionBackgroundColor: Color = Color(176, 176, 176),
    inactiveSelectionForegroundColor: Color = Color.WHITE,
    cursorColor: Color = Color.WHITE
) : RFUTextInput(
    placeholder,
    shadow,
    selectionBackgroundColor,
    selectionForegroundColor,
    allowInactiveSelection,
    inactiveSelectionBackgroundColor,
    inactiveSelectionForegroundColor,
    cursorColor
) {
    protected var minWidth: WidthConstraint? = null
    protected var maxWidth: WidthConstraint? = null

    protected val placeholderWidth = placeholder.width()

    fun setMinWidth(constraint: WidthConstraint) = apply {
        minWidth = constraint
    }

    fun setMaxWidth(constraint: WidthConstraint) = apply {
        maxWidth = constraint
    }

    override fun getText() = textualLines.first().text

    protected open fun getTextForRender(): String = getText()

    protected open fun setCursorPos() {
        cursorComponent.unhide()
        val (cursorPosX, _) = cursor.toScreenPos()
        cursorComponent.setX((cursorPosX).pixels())
    }

    override fun textToLines(text: String): List<String> {
        return listOf(text.replace('\n', ' '))
    }

    override fun scrollIntoView(pos: LinePosition) {
        val column = pos.column
        val lineText = getTextForRender()
        if (column < 0 || column > lineText.length)
            return

        val widthBeforePosition = lineText.substring(0, column).width(getTextScale())

        when {
            getTextForRender().width(getTextScale()) < getWidth() -> {
                horizontalScrollingOffset = 0f
            }
            horizontalScrollingOffset > widthBeforePosition -> {
                horizontalScrollingOffset = widthBeforePosition
            }
            widthBeforePosition - horizontalScrollingOffset > getWidth() -> {
                horizontalScrollingOffset = widthBeforePosition - getWidth()
            }
        }
    }

    override fun screenPosToVisualPos(x: Float, y: Float): LinePosition {
        val targetXPos = x + horizontalScrollingOffset
        var currentX = 0f

        val line = getTextForRender()
        emojiColumnAtX(line, targetXPos)?.let { return LinePosition(0, it, isVisual = true) }

        for (i in line.indices) {
            val charWidth = line[i].width(getTextScale())
            if (currentX + (charWidth / 2) >= targetXPos) return LinePosition(0, i, isVisual = true)
            currentX += charWidth
        }

        return LinePosition(0, line.length, isVisual = true)
    }

    override fun recalculateDimensions() {
        if (minWidth != null && maxWidth != null) {
            val width = if (!hasText() && !this.active) {
                placeholderWidth
            } else {
                getTextForRender().width(getTextScale()) + 1 /* cursor */
            }
            setWidth(width.pixels().coerceIn(minWidth!!, maxWidth!!))
        }
    }

    override fun splitTextForWrapping(text: String, maxLineWidth: Float): List<String> {
        return listOf(text)
    }

    override fun onEnterPressed() {
        activateAction(getText())
    }

    //New
    private fun getVerticalOffset(): Float {
        val textHeight = 9f * getTextScale()
        return (getHeight() - textHeight) / 2f
    }

    override fun extractComponent(extractor: ElementaExtractor) {
        val verticalOffset = getVerticalOffset()
        val lineY = getTop() + verticalOffset

        if (!active && !hasText()) {
            getFontProvider().extractMcScale(
                extractor,
                placeholder,
                getColor(),
                getLeft(),
                lineY,
                getTextScale(),
                shadow
            )
            super.extractComponent(extractor)
            return
        }

        val lineText = getTextForRender()

        if (active) {
            snapSelectionToEmojis()
            cursorComponent.setY(basicYConstraint {
                lineY
            })
            setCursorPos()
        }

        if (hasSelection()) {
            var currentX = getLeft()
            cursorComponent.hide(instantly = true)

            if (!selectionStart().isAtLineStart) {
                val preSelectionText = lineText.substring(0, selectionStart().column)
                getFontProvider().extractMcScale(
                    extractor, preSelectionText, getColor(), currentX - horizontalScrollingOffset, lineY, getTextScale(), shadow
                )
                currentX += preSelectionText.width(getTextScale())
            }

            val selectedText = lineText.substring(selectionStart().column, selectionEnd().column)
            val selectedTextWidth = selectedText.width(getTextScale())
            extractSelectedText(extractor, selectedText, currentX, currentX + selectedTextWidth, row = 0)
            currentX += selectedTextWidth

            if (!selectionEnd().isAtLineEnd) {
                val postSelectionText = lineText.substring(selectionEnd().column)
                getFontProvider().extractMcScale(
                    extractor, postSelectionText, getColor(), currentX - horizontalScrollingOffset, lineY, getTextScale(), shadow
                )
            }
        } else {
            getFontProvider().extractMcScale(
                extractor,
                lineText,
                getColor(),
                getLeft() - horizontalScrollingOffset,
                lineY,
                getTextScale(),
                shadow
            )
        }

        super.extractComponent(extractor)
    }
}
