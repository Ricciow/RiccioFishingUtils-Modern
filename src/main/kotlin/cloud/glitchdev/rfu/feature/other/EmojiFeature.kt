package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

object EmojiFeature {
    val TRIGGER_TO_CODEPOINT: Map<String, Int> = Emoji.EMOJIS.flatMap { (unicode, aliases) ->
        val cp = unicode.codePointAt(0)
        aliases.map { ":${it.lowercase()}:" to cp }
    }.toMap()

    @JvmStatic
    fun isEmojiCodepoint(codepoint: Int): Boolean {
        return codepoint in 0xE100..0xE1FF
    }

    private val EMOJI_STYLE: Style = Style.EMPTY.withColor(ChatFormatting.WHITE).withShadowColor(0)

    data class EmojiMatch(val start: Int, val end: Int, val emojiCodepoint: Int)

    fun findEmojiMatches(text: String): List<EmojiMatch> {
        if (!text.contains(":")) return emptyList()

        val lowerText = text.lowercase()
        val matches = mutableListOf<EmojiMatch>()
        var searchIndex = 0

        while (searchIndex < lowerText.length) {
            val colonIndex = lowerText.indexOf(':', searchIndex)
            if (colonIndex == -1) break

            val nextColonIndex = lowerText.indexOf(':', colonIndex + 1)
            if (nextColonIndex == -1) break

            val candidate = lowerText.substring(colonIndex, nextColonIndex + 1)
            val emojiCp = TRIGGER_TO_CODEPOINT[candidate]
            if (emojiCp != null) {
                matches.add(EmojiMatch(colonIndex, nextColonIndex + 1, emojiCp))
                searchIndex = nextColonIndex + 1
            } else {
                searchIndex = colonIndex + 1
            }
        }
        return matches
    }

    /**
     * Replaces ALL registered emoji triggers (e.g., :dog:) with their PUA characters in a String.
     */
    fun replaceEmojis(text: String?): String? {
        if (text == null || !OtherSettings.emojis || !text.contains(":")) return text
        
        var result = text
        Emoji.ALL.forEach { (trigger, replacement) ->
            result = result?.replace(trigger, replacement, true)
        }
        return result
    }

    private data class StyledChar(val style: Style, val codepoint: Int)

    /**
     * Replaces emoji triggers in a FormattedCharSequence while preserving the original Style
     * of all non-emoji characters, and setting the emoji character style to WHITE without shadow.
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

        for (match in matches) {
            while (currentIdx < match.start) {
                newChars.add(chars[currentIdx])
                currentIdx++
            }

            newChars.add(StyledChar(EMOJI_STYLE, match.emojiCodepoint))
            currentIdx = match.end
        }

        while (currentIdx < chars.size) {
            newChars.add(chars[currentIdx])
            currentIdx++
        }

        return FormattedCharSequence { sink ->
            var idx = 0
            for (sc in newChars) {
                if (!sink.accept(idx++, sc.style, sc.codepoint)) {
                    return@FormattedCharSequence false
                }
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
                val emojiStr = String(Character.toChars(match.emojiCodepoint))
                val emojiCharSeq = Component.literal(emojiStr).getVisualOrderText()
                val emojiWidth = font.width(emojiCharSeq)
                if (positionInText < currentX + emojiWidth / 2) {
                    return match.start
                } else if (positionInText <= currentX + emojiWidth) {
                    return match.end
                }
                currentX += emojiWidth
                rawIdx = match.end
            } else {
                val charStr = displayed.substring(rawIdx, rawIdx + 1)
                val charWidth = font.width(charStr)
                if (positionInText < currentX + charWidth / 2) {
                    return rawIdx
                }
                currentX += charWidth
                rawIdx++
            }
        }
        return displayed.length
    }
}
