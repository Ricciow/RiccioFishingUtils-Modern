package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.EmojiData
import net.minecraft.client.StringSplitter
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import net.minecraft.util.StringDecomposer
import java.util.Optional

object EmojiFeature {
    private val colonTriggers = Emoji.COLON_TRIGGERS.mapKeys { it.key.lowercase() }
    private val otherTriggers = Emoji.CUSTOM_TRIGGERS.mapKeys { it.key.lowercase() }

    private fun hasPossibleEmoji(text: String): Boolean = text.contains(':') ||
        otherTriggers.keys.any { text.contains(it, ignoreCase = true) }

    @JvmStatic
    fun hasEmojis(text: String): Boolean = findEmojiMatches(text).isNotEmpty()

    data class EmojiMatch(val start: Int, val end: Int, val emoji: EmojiData)

    fun findEmojiMatches(text: String): List<EmojiMatch> {
        if (!OtherSettings.emojis || !hasPossibleEmoji(text)) return emptyList()
        val matches = mutableListOf<EmojiMatch>()
        var searchIndex = 0
        while (searchIndex < text.length) {
            val start = text.indexOf(':', searchIndex)
            if (start == -1) break
            val end = text.indexOf(':', start + 1)
            if (end == -1) break
            val trigger = text.substring(start, end + 1).lowercase()
            val emoji = colonTriggers[trigger]
            if (emoji != null) matches.add(EmojiMatch(start, end + 1, emoji))
            searchIndex = if (emoji != null) end + 1 else start + 1
        }
        for ((trigger, emoji) in otherTriggers) {
            var index = text.indexOf(trigger, ignoreCase = true)
            while (index != -1) {
                val replacement = emoji.textReplacement.orEmpty()
                val triggerOffset = replacement.indexOf(trigger, ignoreCase = true)
                val alreadyExpanded = triggerOffset >= 0 && text.regionMatches(
                    index - triggerOffset, replacement, 0, replacement.length, ignoreCase = true
                )
                if (!alreadyExpanded) matches.add(EmojiMatch(index, index + trigger.length, emoji))
                index = text.indexOf(trigger, index + trigger.length, ignoreCase = true)
            }
        }
        matches.sortBy { it.start }
        var lastEnd = 0
        return matches.filter { match ->
            (match.start >= lastEnd).also { if (it) lastEnd = match.end }
        }
    }

    internal data class StyledChar(val start: Int, val end: Int, val style: Style, val codepoint: Int)

    internal fun readChars(text: String, style: Style, formatted: Boolean): List<StyledChar> {
        val chars = mutableListOf<StyledChar>()
        val sink = FormattedCharSink { index, charStyle, codepoint ->
            chars.add(StyledChar(index, index + Character.charCount(codepoint), charStyle, codepoint))
            true
        }
        if (formatted) StringDecomposer.iterateFormatted(text, style, sink)
        else StringDecomposer.iterate(text, style, sink)
        return chars
    }

    internal fun replaceChars(chars: List<StyledChar>, matches: List<EmojiMatch>): List<StyledChar> {
        val result = mutableListOf<StyledChar>()
        var index = 0
        for (match in matches) {
            while (index < chars.size && chars[index].start < match.start) result.add(chars[index++])
            val style = chars[index].style
            if (match.emoji.font != null) {
                result.add(StyledChar(match.start, match.end, match.emoji.spriteStyle(style), 0xFFFC))
            } else {
                StringDecomposer.iterateFormatted(match.emoji.text, style) { _, replacementStyle, codepoint ->
                    result.add(StyledChar(match.start, match.end, replacementStyle, codepoint))
                    true
                }
            }
            while (index < chars.size && chars[index].start < match.end) index++
        }
        result.addAll(chars.subList(index, chars.size))
        return result
    }

    private fun sequence(chars: List<StyledChar>): FormattedCharSequence = FormattedCharSequence { sink ->
        var index = 0
        for (char in chars) {
            if (!sink.accept(index, char.style, char.codepoint)) return@FormattedCharSequence false
            index += Character.charCount(char.codepoint)
        }
        true
    }

    /** Returns null when vanilla can render the original string unchanged. */
    @JvmStatic
    fun replaceEmojisInString(text: String, style: Style): FormattedCharSequence? {
        val matches = findEmojiMatches(text)
        if (matches.isEmpty()) return null
        return sequence(replaceChars(readChars(text, style, formatted = true), matches))
    }

    /** Replace logical text before wrapping and bidi ordering; keep unstyled access to the original text. */
    @JvmStatic
    fun replaceEmojisInText(text: FormattedText): FormattedText {
        if (!OtherSettings.emojis) return text
        return object : FormattedText {
            override fun <T : Any> visit(output: FormattedText.ContentConsumer<T>): Optional<T> = text.visit(output)

            override fun <T : Any> visit(output: FormattedText.StyledContentConsumer<T>, parentStyle: Style): Optional<T> =
                text.visit({ style, contents ->
                    val matches = findEmojiMatches(contents)
                    if (matches.isEmpty()) output.accept(style, contents)
                    else {
                        val chars = replaceChars(readChars(contents, style, formatted = true), matches)
                        var start = 0
                        var result = Optional.empty<T>()
                        while (start < chars.size && result.isEmpty) {
                            val runStyle = chars[start].style
                            val run = StringBuilder()
                            do {
                                run.appendCodePoint(chars[start++].codepoint)
                            } while (start < chars.size && chars[start].style == runStyle)
                            result = output.accept(runStyle, run.toString())
                        }
                        result
                    }
                }, parentStyle)
        }
    }

    /** Used for input formatters and callers that already supply a character sequence. */
    @JvmStatic
    fun replaceEmojisInCharSequence(input: FormattedCharSequence): FormattedCharSequence {
        if (!OtherSettings.emojis) return input
        val chars = mutableListOf<StyledChar>()
        val text = StringBuilder()
        input.accept { _, style, codepoint ->
            val start = text.length
            text.appendCodePoint(codepoint)
            chars.add(StyledChar(start, text.length, style, codepoint))
            true
        }
        val matches = findEmojiMatches(text.toString())
        return if (matches.isEmpty()) input else sequence(replaceChars(chars, matches))
    }

    /** Raw UTF-16 positions remain valid when an entire trigger occupies a single sprite cell. */
    @JvmStatic
    fun plainIndexAtWidth(text: String, width: Int, style: Style, provider: StringSplitter.WidthProvider, reverse: Boolean): Int? {
        val matches = findEmojiMatches(text)
        if (matches.isEmpty()) return null
        val chars = replaceChars(readChars(text, style, formatted = false), matches)
        var position = if (reverse) text.length else 0
        var remaining = width.toFloat()
        val runs = chars.groupBy { it.start to it.end }.values
        for (run in if (reverse) runs.reversed() else runs) {
            val advance = run.sumOf { provider.getWidth(it.codepoint, it.style).toDouble() }.toFloat()
            if (advance > remaining) break
            remaining -= advance
            position = if (reverse) run.first().start else run.last().end
        }
        return position
    }

    @JvmStatic
    fun snapToEmojiBoundary(text: String?, pos: Int, preferEnd: Boolean): Int {
        if (text == null) return pos
        val match = findEmojiMatches(text).firstOrNull { pos in (it.start + 1)..<it.end } ?: return pos
        return if (preferEnd) match.end else match.start
    }

    @JvmStatic
    fun getClickedRawPosition(font: Font, displayed: String, positionInText: Int): Int {
        if (positionInText <= 0) return font.plainSubstrByWidth(displayed, positionInText).length
        val matches = findEmojiMatches(displayed)
        if (matches.isEmpty()) return font.plainSubstrByWidth(displayed, positionInText).length
        val chars = replaceChars(readChars(displayed, Style.EMPTY, formatted = false), matches)
        var x = 0
        for (run in chars.groupBy { it.start to it.end }.values) {
            val advance = font.width(sequence(run))
            if (positionInText < x + advance / 2f) return run.first().start
            if (positionInText <= x + advance) return run.last().end
            x += advance
        }
        return displayed.length
    }
}
