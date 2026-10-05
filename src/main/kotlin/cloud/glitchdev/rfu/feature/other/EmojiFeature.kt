package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.Emoji.whiteText
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.StringDecomposer

object EmojiFeature {
    val COLON_TRIGGERS: Map<String, String> = Emoji.COLON_TRIGGERS.map { (trigger, replacement) ->
        trigger.lowercase() to replacement
    }.toMap()

    val OTHER_TRIGGERS: Map<String, String> = Emoji.CUSTOM_TRIGGERS.map { (trigger, replacement) ->
        trigger.lowercase() to replacement
    }.toMap()

    private fun hasPossibleEmoji(text: String): Boolean {
        if (text.contains(':')) return true
        return OTHER_TRIGGERS.keys.any { text.contains(it, ignoreCase = true) }
    }

    @JvmStatic
    fun isEmojiCodepoint(codepoint: Int): Boolean {
        return codepoint in 0xE100..0xE1FF
    }

    private val EMOJI_STYLE: Style = Style.EMPTY.withColor(ChatFormatting.WHITE).withShadowColor(0)
    private val REPLACEMENT_STYLE: Style = Style.EMPTY.withColor(ChatFormatting.WHITE)

    data class EmojiMatch(val start: Int, val end: Int, val replacement: String)

    fun findEmojiMatches(text: String): List<EmojiMatch> {
        if (text.isEmpty() || !hasPossibleEmoji(text)) return emptyList()
        val matches = mutableListOf<EmojiMatch>()

        if (text.contains(':')) {
            var searchIndex = 0
            while (searchIndex < text.length) {
                val colonIndex = text.indexOf(':', searchIndex)
                if (colonIndex == -1) break

                val nextColonIndex = text.indexOf(':', colonIndex + 1)
                if (nextColonIndex == -1) break

                val candidate = text.substring(colonIndex, nextColonIndex + 1).lowercase()
                val replacement = COLON_TRIGGERS[candidate]
                if (replacement != null) {
                    matches.add(EmojiMatch(colonIndex, nextColonIndex + 1, replacement))
                    searchIndex = nextColonIndex + 1
                } else {
                    searchIndex = colonIndex + 1
                }
            }
        }

        for ((trigger, replacement) in OTHER_TRIGGERS) {
            var idx = text.indexOf(trigger, ignoreCase = true)
            while (idx != -1) {
                val triggerOffset = replacement.indexOf(trigger, ignoreCase = true)
                val alreadyExpanded = triggerOffset >= 0 && text.regionMatches(
                    idx - triggerOffset, replacement, 0, replacement.length, ignoreCase = true
                )
                if (!alreadyExpanded) {
                    matches.add(EmojiMatch(idx, idx + trigger.length, replacement))
                }
                idx = text.indexOf(trigger, idx + trigger.length, ignoreCase = true)
            }
        }

        if (matches.size <= 1) return matches

        matches.sortBy { it.start }

        val nonOverlapping = mutableListOf<EmojiMatch>()
        var lastEnd = 0
        for (match in matches) {
            if (match.start >= lastEnd) {
                nonOverlapping.add(match)
                lastEnd = match.end
            }
        }
        return nonOverlapping
    }

    /**
     * Replaces ALL registered emoji triggers (e.g., :dog:, (ᵔᴥᵔ)) with their PUA characters in a String.
     */
    fun replaceEmojis(text: String?): String? {
        if (text == null || !OtherSettings.emojis || !hasPossibleEmoji(text)) return text
        return replaceMatches(text, formatted = true)
    }

    fun replaceEmojisUnformatted(text: String?): String? {
        if (text == null || !hasPossibleEmoji(text)) return text
        return replaceMatches(text, formatted = false)
    }

    private fun replaceMatches(text: String, formatted: Boolean): String {
        val matches = findEmojiMatches(text)
        if (matches.isEmpty()) return text
        return buildString {
            var currentIndex = 0
            for (match in matches) {
                append(text, currentIndex, match.start)
                append(if (formatted) match.replacement.whiteText() else match.replacement)
                currentIndex = match.end
            }
            append(text, currentIndex, text.length)
        }
    }

    fun clearAndApplyPostStyle(text: String?, style: TextStyle?): String? {
        var result = text
        Emoji.ALL.forEach { (_, replacement) ->
            result = result?.replace(replacement, "${TextColor.WHITE}$replacement${TextEffects.RESET}${style?:""}", true)
        }
        return result
    }

    private data class StyledChar(val style: Style, val codepoint: Int)

    private fun replacementSequence(replacement: String): FormattedCharSequence = FormattedCharSequence { sink ->
        StringDecomposer.iterateFormatted(replacement, REPLACEMENT_STYLE) { index, style, codepoint ->
            sink.accept(index, if (isEmojiCodepoint(codepoint)) EMOJI_STYLE else style, codepoint)
        }
    }

    /**
     * Replaces emoji triggers in a FormattedCharSequence while preserving the original Style
     * of surrounding characters. Glyphs are white without shadow; text uses its own formatting.
     */
    fun replaceEmojisInCharSequence(sequence: FormattedCharSequence?): FormattedCharSequence? {
        if (sequence == null || !OtherSettings.emojis) return sequence

        val chars = mutableListOf<StyledChar>()
        sequence.accept { _, style, codepoint ->
            chars.add(StyledChar(style, codepoint))
            true
        }

        if (chars.isEmpty()) return sequence

        val sb = StringBuilder()
        for (c in chars) {
            sb.appendCodePoint(c.codepoint)
        }
        val fullText = sb.toString()
        val matches = findEmojiMatches(fullText)
        if (matches.isEmpty()) return sequence

        val newChars = mutableListOf<StyledChar>()
        var currentIdx = 0
        var currentOffset = 0

        for (match in matches) {
            while (currentOffset < match.start) {
                val char = chars[currentIdx++]
                newChars.add(char)
                currentOffset += Character.charCount(char.codepoint)
            }

            replacementSequence(match.replacement).accept { _, style, codepoint ->
                newChars.add(StyledChar(style, codepoint))
                true
            }
            while (currentOffset < match.end) {
                currentOffset += Character.charCount(chars[currentIdx++].codepoint)
            }
        }

        while (currentIdx < chars.size) {
            newChars.add(chars[currentIdx])
            currentIdx++
        }

        return FormattedCharSequence { sink ->
            var idx = 0
            for (sc in newChars) {
                if (!sink.accept(idx, sc.style, sc.codepoint)) {
                    return@FormattedCharSequence false
                }
                idx += Character.charCount(sc.codepoint)
            }
            true
        }
    }

    @JvmStatic
    fun snapToEmojiBoundary(text: String?, pos: Int, preferEnd: Boolean): Int {
        if (text == null || !OtherSettings.emojis) return pos

        val matches = findEmojiMatches(text)
        for (match in matches) {
            if (pos in (match.start + 1)..<match.end) {
                return if (preferEnd) match.end else match.start
            }
        }
        return pos
    }

    @JvmStatic
    fun getClickedRawPosition(font: Font, displayed: String, positionInText: Int): Int {
        if (!OtherSettings.emojis || positionInText <= 0) {
            return font.plainSubstrByWidth(displayed, positionInText).length
        }

        val matches = findEmojiMatches(displayed)
        if (matches.isEmpty()) {
            return font.plainSubstrByWidth(displayed, positionInText).length
        }

        var currentX = 0
        var rawIdx = 0
        while (rawIdx < displayed.length) {
            val match = matches.firstOrNull { it.start == rawIdx }
            if (match != null) {
                val emojiWidth = font.width(replacementSequence(match.replacement))
                if (positionInText < currentX + emojiWidth / 2) {
                    return match.start
                } else if (positionInText <= currentX + emojiWidth) {
                    return match.end
                }
                currentX += emojiWidth
                rawIdx = match.end
            } else {
                val charLength = Character.charCount(displayed.codePointAt(rawIdx))
                val charStr = displayed.substring(rawIdx, rawIdx + charLength)
                val charWidth = font.width(charStr)
                if (positionInText < currentX + charWidth / 2) {
                    return rawIdx
                }
                currentX += charWidth
                rawIdx += charLength
            }
        }
        return displayed.length
    }
}
