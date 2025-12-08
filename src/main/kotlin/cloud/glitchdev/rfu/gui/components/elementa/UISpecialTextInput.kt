package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.elementa.constraints.WidthConstraint
import gg.essential.elementa.dsl.basicYConstraint
import gg.essential.elementa.dsl.coerceIn
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width
import gg.essential.universal.UMatrixStack
import java.awt.Color

open class UISpecialTextInput @JvmOverloads constructor(
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

    private fun getVerticalOffset(): Float {
        val textHeight = 9f * getTextScale()
        return (getHeight() - textHeight) / 2f
    }

    override fun draw(matrixStack: UMatrixStack) {
        beforeDrawCompat(matrixStack)

        val verticalOffset = getVerticalOffset()

        if (!active && !hasText()) {
            getFontProvider().drawString(
                matrixStack,
                placeholder,
                getColor(),
                getLeft(),
                getTop() + verticalOffset,
                10f,
                getTextScale(),
                shadow
            )
            return super.draw(matrixStack)
        }

        val lineText = getTextForRender()

        if (active) {
            cursorComponent.setY(basicYConstraint {
                getTop() + verticalOffset
            })
            setCursorPos()
        }

        matrixStack.push()
        matrixStack.translate(0.0, verticalOffset.toDouble(), 0.0)

        if (hasSelection()) {
            var currentX = getLeft()
            cursorComponent.hide(instantly = true)

            if (!selectionStart().isAtLineStart) {
                val preSelectionText = lineText.substring(0, selectionStart().column)

                getFontProvider().drawString(
                    matrixStack, preSelectionText, getColor(), currentX, getTop(), 10f, getTextScale(), shadow
                )

                currentX += preSelectionText.width(getTextScale())
            }

            val selectedText = lineText.substring(selectionStart().column, selectionEnd().column)
            val selectedTextWidth = selectedText.width(getTextScale())
            drawSelectedTextCompat(matrixStack, selectedText, currentX, currentX + selectedTextWidth, row = 0)
            currentX += selectedTextWidth

            if (!selectionEnd().isAtLineEnd) {
                val postSelectionText = lineText.substring(selectionEnd().column)

                getFontProvider().drawString(
                    matrixStack, postSelectionText, getColor(), currentX, getTop(), 10f, getTextScale(), shadow
                )
            }

        } else {
            getFontProvider().drawString(
                matrixStack,
                lineText,
                getColor(),
                getLeft(),
                getTop(),
                10f,
                getTextScale(),
                shadow
            )
        }

        matrixStack.pop()

        super.draw(matrixStack)
    }
}