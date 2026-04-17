/*
 * Copyright (C) 2026 EssentialGG (Elementa)
 * Copyright (C) 2026 Riccio (Modifications)
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

import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.dsl.coerceAtMost
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width
import gg.essential.elementa.utils.splitStringToWidthTruncated
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import java.awt.Color

class UISpecialMultilineTextInput @JvmOverloads constructor(
    placeholder: String = "",
    shadow: Boolean = true,
    selectionBackgroundColor: Color = Color.WHITE,
    selectionForegroundColor: Color = Color(64, 139, 229),
    allowInactiveSelection: Boolean = false,
    inactiveSelectionBackgroundColor: Color = Color(176, 176, 176),
    inactiveSelectionForegroundColor: Color = Color.WHITE,
    cursorColor: Color = Color.WHITE
) : AbstractTextInput(
    placeholder,
    shadow,
    selectionBackgroundColor,
    selectionForegroundColor,
    allowInactiveSelection,
    inactiveSelectionBackgroundColor,
    inactiveSelectionForegroundColor,
    cursorColor
) {
    private var maxHeight: HeightConstraint? = null

    fun setMaxHeight(maxHeight: HeightConstraint) = apply {
        this.maxHeight = maxHeight
    }

    fun setMaxLines(maxLines: Int) = apply {
        this.maxHeight = (lineHeight * maxLines).pixels()
    }

    override fun getText() = textualLines.joinToString("\n") { it.text }

    override fun textToLines(text: String): List<String> {
        return text.split('\n')
    }

    override fun scrollIntoView(pos: LinePosition) {
        val visualPos = pos.toVisualPos()

        val visualLineOffset = visualPos.line * -lineHeight

        if (targetVerticalScrollingOffset < visualLineOffset) {
            targetVerticalScrollingOffset = visualLineOffset
        } else if (visualLineOffset - lineHeight < targetVerticalScrollingOffset - getHeight()) {
            targetVerticalScrollingOffset += visualLineOffset - lineHeight - (targetVerticalScrollingOffset - getHeight())
        }
    }

    override fun recalculateDimensions() {
        if (maxHeight == null)
            return

        setHeight((lineHeight * visualLines.size).pixels().coerceAtMost(maxHeight!!))
    }

    override fun onEnterPressed() {
        if (UKeyboard.isShiftKeyDown()) {
            commitTextAddition("\n")
            updateAction(getText())
        } else {
            activateAction(getText())
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)

        val textScale = getTextScale()
        if (!active && !hasText()) {
            val textToDraw = splitStringToWidthTruncated(placeholder, getWidth(), textScale, 1)[0]
            // MODIFIED: Draw placeholder with shadow
            getFontProvider().drawString(
                matrixStack, textToDraw, getColor(), getLeft(), getTop(), 10f, textScale, shadow
            )
            return super.draw(matrixStack)
        }

        if (hasSelection()) {
            cursorComponent.hide(instantly = true)
        } else if (active) {
            cursorComponent.unhide()
            val (cursorPosX, cursorPosY) = cursor.toScreenPos()
            cursorComponent.setX((cursorPosX).pixels())
            cursorComponent.setY((cursorPosY).pixels())
        }

        val (selectionStart, selectionEnd) = getSelection()

        for ((i, visualLine) in visualLines.withIndex()) {
            val topOffset = (lineHeight * i * textScale) + verticalScrollingOffset
            if (topOffset < -lineHeight * textScale || topOffset > getHeight() + lineHeight * textScale)
                continue

            // Calculate the absolute Y position for the line
            val lineY = getTop() + topOffset

            if (!hasSelection() || i < selectionStart.line || i > selectionEnd.line) {
                // MODIFIED: Draw standard line text with shadow
                getFontProvider().drawString(
                    matrixStack, visualLine.text, getColor(), getLeft(), lineY, 10f, textScale, shadow
                )
            } else {
                val startText = when {
                    i == selectionStart.line && selectionStart.column > 0 -> {
                        visualLine.text.substring(0, selectionStart.column)
                    }
                    else -> ""
                }

                val selectedText = when {
                    selectionStart.line == selectionEnd.line -> visualLine.text.substring(
                        selectionStart.column,
                        selectionEnd.column
                    )
                    i > selectionStart.line && i < selectionEnd.line -> visualLine.text
                    i == selectionStart.line -> visualLine.text.substring(selectionStart.column)
                    i == selectionEnd.line -> visualLine.text.substring(0, selectionEnd.column)
                    else -> ""
                }

                val endText = when {
                    i == selectionEnd.line && selectionEnd.column < visualLines[i].length -> {
                        visualLine.text.substring(selectionEnd.column)
                    }
                    else -> ""
                }

                val startTextWidth = startText.width(textScale)
                val selectedTextWidth = selectedText.width(textScale)

                val newlinePadding = if (i < selectionEnd.line) ' '.width(textScale) else 0f

                if (startText.isNotEmpty()) {
                    // MODIFIED: Draw pre-selection text with shadow
                    getFontProvider().drawString(
                        matrixStack, startText, getColor(), getLeft(), lineY, 10f, textScale, shadow
                    )
                }

                if (selectedText.isNotEmpty() || newlinePadding != 0f) {
                    drawSelectedText(
                        matrixStack,
                        selectedText,
                        getLeft() + startTextWidth,
                        getLeft() + startTextWidth + selectedTextWidth + newlinePadding,
                        i
                    )
                }

                if (endText.isNotEmpty()) {
                    // MODIFIED: Draw post-selection text with shadow
                    getFontProvider().drawString(
                        matrixStack, endText, getColor(), getLeft() + startTextWidth + selectedTextWidth, lineY, 10f, textScale, shadow
                    )
                }
            }
        }

        super.draw(matrixStack)
    }

    override fun screenPosToVisualPos(x: Float, y: Float): LinePosition {
        val realY = y - verticalScrollingOffset

        if (realY <= 0)
            return LinePosition(0, 0, isVisual = true)

        val line = (realY / (lineHeight * getTextScale())).toInt()
        if (line > visualLines.lastIndex)
            return LinePosition(visualLines.lastIndex, visualLines.last().text.length, isVisual = true)

        val text = visualLines[line].text
        var column = 0
        var currWidth = 0f

        if (x <= 0)
            return LinePosition(line, 0, isVisual = true)
        if (x >= getWidth())
            return LinePosition(line, visualLines[line].text.length, isVisual = true)

        for (char in text.toCharArray()) {
            val charWidth = char.width(getTextScale())
            if (currWidth + (charWidth / 2) >= x)
                return LinePosition(line, column, isVisual = true)

            currWidth += charWidth
            column++
        }

        return LinePosition(line, column, isVisual = true)
    }
}