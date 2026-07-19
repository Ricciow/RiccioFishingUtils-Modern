package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

object EmojiFeature {
    val TRIGGER_TO_CODEPOINT: Map<String, Int> = Emoji.EMOJIS.flatMap { (unicode, aliases) ->
        val cp = unicode.codePointAt(0)
        aliases.map { ":${it.lowercase()}:" to cp }
    }.toMap()

    private val EMOJI_STYLE: Style = Style.EMPTY.withColor(ChatFormatting.WHITE)

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
     * of all non-emoji characters, and setting the emoji character style to WHITE.
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
        if (!fullText.contains(":")) return sequence

        val lowerText = fullText.lowercase()

        class Match(val start: Int, val end: Int, val emojiCodepoint: Int)
        val matches = mutableListOf<Match>()

        var searchIndex = 0
        while (searchIndex < lowerText.length) {
            val colonIndex = lowerText.indexOf(':', searchIndex)
            if (colonIndex == -1) break

            val nextColonIndex = lowerText.indexOf(':', colonIndex + 1)
            if (nextColonIndex == -1) break

            val candidate = lowerText.substring(colonIndex, nextColonIndex + 1)
            val emojiCp = TRIGGER_TO_CODEPOINT[candidate]
            if (emojiCp != null) {
                matches.add(Match(colonIndex, nextColonIndex + 1, emojiCp))
                searchIndex = nextColonIndex + 1
            } else {
                searchIndex = colonIndex + 1
            }
        }

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
}
