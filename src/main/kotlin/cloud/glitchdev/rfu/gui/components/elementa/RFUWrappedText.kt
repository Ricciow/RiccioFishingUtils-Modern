/*
 * Copyright (C) 2026 EssentialGG (Elementa)
 * Copyright (C) 2026 Riccio (Modifications)
 * Adapted from Elementa's UIWrappedText.
 */

package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.feature.other.EmojiFeature
import gg.essential.elementa.VanillaFontRenderer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.basicHeightConstraint
import gg.essential.elementa.dsl.width
import gg.essential.elementa.font.FontProvider
import gg.essential.elementa.font.extractMcScale
import gg.essential.elementa.renderer.ElementaExtractor
import gg.essential.elementa.utils.getStringSplitToWidth
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.StringSplitter
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.awt.Color
import kotlin.math.floor

/** Wrapped text for RFU screens that can contain inline emoji sprites. */
class RFUWrappedText(
    text: String = "",
    shadow: Boolean = true,
    shadowColor: Color? = null,
    private val centered: Boolean = false,
) : UIWrappedText(text, shadow, shadowColor, centered) {
    init {
        setHeight(basicHeightConstraint {
            val lines = getWrappedLines()
            if (lines.isEmpty()) return@basicHeightConstraint 0f
            val font = getFontProvider()
            val lastLineHeight = font.getBaseLineHeight() + font.getBelowLineHeight() +
                (if (getShadow()) font.getShadowHeight() else 0f)
            val topPadding = if (constraints.y is CenterConstraint) font.getBelowLineHeight() else 0f
            ((lines.size - 1) * 9f + lastLineHeight + topPadding) * getTextScale()
        })
    }

    fun getWrappedLines(): List<String> = wrapText(
        getText(), getWidth(), getTextScale(), getFontProvider(), ensureSpaceAtEndOfLines = false
    ).map { it.trimEnd() }

    override fun extractComponent(extractor: ElementaExtractor) {
        val scale = getTextScale()
        val font = getFontProvider()
        val y = getTop() + if (constraints.y is CenterConstraint) font.getBelowLineHeight() * scale else 0f
        getWrappedLines().forEachIndexed { index, line ->
            val xOffset = if (centered) (getWidth() - line.width(scale, font)) / 2f else 0f
            font.extractMcScale(
                extractor, line, getColor(), getLeft() + xOffset, y + index * 9f * scale,
                scale, getShadow(), if (getShadow()) getShadowColor() else null
            )
        }
    }

    companion object {
        fun wrapText(
            text: String,
            maxLineWidth: Float,
            scale: Float,
            fontProvider: FontProvider,
            ensureSpaceAtEndOfLines: Boolean = true,
            processColorCodes: Boolean = true,
        ): List<String> {
            if (fontProvider !is VanillaFontRenderer || scale <= 0f || !EmojiFeature.hasEmojis(text)) {
                return getStringSplitToWidth(text, maxLineWidth, scale, ensureSpaceAtEndOfLines, processColorCodes, fontProvider)
            }
            val font = Minecraft.getInstance().font
            val width = floor(maxLineWidth / scale - if (ensureSpaceAtEndOfLines) font.width(" ") else 0).toInt().coerceAtLeast(1)
            if (processColorCodes) return splitStringToWidth(text, width, font.splitter)

            val lines = mutableListOf<String>()
            var start = 0
            while (start < text.length) {
                val remaining = text.substring(start)
                val head = font.plainSubstrByWidth(remaining, width)
                val length = when {
                    head.length == remaining.length -> head.length
                    head.isEmpty() -> EmojiFeature.findEmojiMatches(remaining).firstOrNull { it.start == 0 }?.end
                        ?: Character.charCount(remaining.codePointAt(0))
                    head.contains(' ') -> head.lastIndexOf(' ') + 1
                    else -> head.length
                }
                lines.add(remaining.substring(0, length))
                start += length
            }
            return lines.ifEmpty { listOf("") }
        }

        private fun splitStringToWidth(text: String, width: Int, splitter: StringSplitter): List<String> {
            var source = text.replace(Regex("&([0-9a-fk-or])"), "§$1")
            val textMatches = EmojiFeature.findEmojiMatches(source).filter { it.emoji.font == null }
            if (textMatches.isNotEmpty()) {
                source = buildString {
                    var end = 0
                    for (match in textMatches) {
                        append(source, end, match.start)
                        append(match.emoji.text)
                        end = match.end
                    }
                    append(source, end, source.length)
                }
            }
            val sourceChars = EmojiFeature.readChars(source, Style.EMPTY, formatted = true)
            val chars = EmojiFeature.replaceChars(sourceChars, EmojiFeature.findEmojiMatches(source))
            val parts = chars.map { FormattedText.of(Character.toString(it.codepoint), it.style) }
            val lines = splitter.splitLines(FormattedText.composite(parts), width, Style.EMPTY)
            var charIndex = 0
            return lines.map { line ->
                val lineText = line.string
                val count = lineText.codePointCount(0, lineText.length)
                val start = charIndex
                charIndex += count
                val rawStart = if (start == 0) 0 else chars.getOrNull(start)?.start ?: source.length
                val raw = if (count == 0) "" else source.substring(rawStart, chars[charIndex - 1].end)
                val sourceStyle = sourceChars.firstOrNull { it.start >= rawStart }?.style ?: Style.EMPTY
                val prefix = if (count > 0) legacyStyle(sourceStyle) else ""
                if (charIndex < chars.size && (chars[charIndex].codepoint == 32 || chars[charIndex].codepoint == 10)) charIndex++
                prefix + raw
            }
        }

        private fun legacyStyle(style: Style): String = buildString {
            append(ChatFormatting.RESET)
            if (style.color != null) {
                ChatFormatting.entries.firstOrNull { TextColor.fromLegacyFormat(it) == style.color }?.let { append(it) }
            }
            if (style.isBold) append(ChatFormatting.BOLD)
            if (style.isItalic) append(ChatFormatting.ITALIC)
            if (style.isUnderlined) append(ChatFormatting.UNDERLINE)
            if (style.isStrikethrough) append(ChatFormatting.STRIKETHROUGH)
            if (style.isObfuscated) append(ChatFormatting.OBFUSCATED)
        }
    }
}
