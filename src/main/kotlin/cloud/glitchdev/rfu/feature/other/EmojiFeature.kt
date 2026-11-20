package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.EmojiData
import cloud.glitchdev.rfu.data.other.EmojiPreferences
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerModifyChatEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerModifyCommandEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import net.minecraft.client.StringSplitter
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import net.minecraft.util.StringDecomposer
import net.minecraft.util.StringUtil
import java.util.Optional

@RFUFeature
object EmojiFeature : Feature {
    private val colonTriggers = Emoji.COLON_TRIGGERS.mapKeys { it.key.lowercase() }
    private val otherTriggers = Emoji.CUSTOM_TRIGGERS.mapKeys { it.key.lowercase() }
    private val outgoingTrigger = Emoji.MESSAGE_REPLACEMENTS.keys
        .sortedByDescending { it.length }
        .joinToString("|") { trigger ->
            val literal = Regex.escape(trigger)
            if (trigger.startsWith(':') && trigger.endsWith(':')) literal else "(?<!\\S)$literal(?!\\S)"
        }
        .takeIf { it.isNotEmpty() }
        ?.toRegex(RegexOption.IGNORE_CASE)

    private val singleArgChatCommands = setOf(
        "ac", "achat", "allchat",
        "pc", "pchat", "partychat",
        "gc", "gchat", "guildchat",
        "oc", "ochat", "officerchat",
        "cc", "cchat", "coopchat",
        "r", "reply", "shout",
    )
    private val whisperCommands = setOf("msg", "w", "tell", "message", "whisper")
    private val subChatCommands = setOf("g", "guild", "p", "party", "co", "coop")

    override fun onInitialize() {
        registerModifyChatEvent { message ->
            if (!OtherSettings.emojis) return@registerModifyChatEvent message
            val expanded = expandOutgoing(message)
            if (StringUtil.trimChatMessage(expanded) == expanded) expanded else message
        }

        registerModifyCommandEvent { command ->
            if (!OtherSettings.emojis) return@registerModifyCommandEvent command
            val messageStart = chatCommandMessageStart(command) ?: return@registerModifyCommandEvent command
            val expanded = command.substring(0, messageStart) + expandOutgoing(command.substring(messageStart))
            if (StringUtil.trimChatMessage(expanded) == expanded) expanded else command
        }
    }

    private fun expandOutgoing(message: String): String {
        return outgoingTrigger?.replace(message) { match ->
            Emoji.MESSAGE_REPLACEMENTS[match.value.lowercase()]
                ?.takeIf(EmojiPreferences::isEnabled)
                ?.messageReplacement ?: match.value
        } ?: message
    }

    private fun chatCommandMessageStart(command: String): Int? {
        val firstSpace = command.indexOf(' ')
        if (firstSpace <= 0) return null
        val name = command.substring(0, firstSpace).lowercase()
        val firstArgStart = command.indexOfFirstAfter(firstSpace) { it != ' ' } ?: return null
        if (name in singleArgChatCommands) return firstArgStart

        if (name in whisperCommands || name in subChatCommands) {
            val secondSpace = command.indexOf(' ', firstArgStart)
            if (secondSpace == -1) return null
            if (name in subChatCommands && !command.substring(firstArgStart, secondSpace).equals("chat", ignoreCase = true)) {
                return null
            }
            return command.indexOfFirstAfter(secondSpace) { it != ' ' }
        }
        return null
    }

    private inline fun String.indexOfFirstAfter(startIndex: Int, predicate: (Char) -> Boolean): Int? {
        for (i in startIndex until length) {
            if (predicate(this[i])) return i
        }
        return null
    }

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
            val emoji = colonTriggers[trigger]?.takeIf(EmojiPreferences::isEnabled)
            if (emoji != null) matches.add(EmojiMatch(start, end + 1, emoji))
            searchIndex = if (emoji != null) end + 1 else start + 1
        }
        for ((trigger, emoji) in otherTriggers) {
            if (!EmojiPreferences.isEnabled(emoji)) continue
            val wordOnly = trigger.all { it.isLetterOrDigit() }
            val messageShortcut = emoji.messageReplacement != null && !trigger.equals(emoji.messageReplacement, ignoreCase = true)
            var index = text.indexOf(trigger, ignoreCase = true)
            while (index != -1) {
                val end = index + trigger.length
                val bounded = when {
                    messageShortcut -> (index == 0 || text[index - 1].isWhitespace()) &&
                        (end == text.length || text[end].isWhitespace())
                    wordOnly -> (index == 0 || !text[index - 1].isLetterOrDigit() || (index >= 2 && text[index - 2] == '§')) &&
                        (end == text.length || !text[end].isLetterOrDigit())
                    else -> true
                }
                val replacement = emoji.textReplacement.orEmpty()
                val triggerOffset = replacement.indexOf(trigger, ignoreCase = true)
                val alreadyExpanded = triggerOffset >= 0 && text.regionMatches(
                    index - triggerOffset, replacement, 0, replacement.length, ignoreCase = true
                )
                if (bounded && !alreadyExpanded) matches.add(EmojiMatch(index, end, emoji))
                index = text.indexOf(trigger, end, ignoreCase = true)
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
    fun getClickedRawPosition(font: Font, displayed: String, positionInText: Int): Int =
        getClickedRawPosition(font, displayed, positionInText.toFloat())

    fun getClickedRawPosition(font: Font, displayed: String, positionInText: Float): Int {
        if (positionInText <= 0) return 0
        val matches = findEmojiMatches(displayed)
        if (matches.isEmpty()) return font.plainSubstrByWidth(displayed, positionInText.toInt()).length
        val chars = replaceChars(readChars(displayed, Style.EMPTY, formatted = false), matches)
        var x = 0f
        for (run in chars.groupBy { it.start to it.end }.values) {
            val advance = font.width(sequence(run))
            if (positionInText < x + advance / 2f) return run.first().start
            if (positionInText <= x + advance) return run.last().end
            x += advance
        }
        return displayed.length
    }
}
