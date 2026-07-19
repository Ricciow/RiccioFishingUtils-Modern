package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestions
import java.util.concurrent.CompletableFuture

object EmojiAutocomplete {
    @JvmStatic
    fun getEmojiSuggestions(input: String, cursorPosition: Int): CompletableFuture<Suggestions>? {
        if (!OtherSettings.emojis) return null

        val partial = input.substring(0, cursorPosition)
        val lastSpace = partial.indexOfLast { it.isWhitespace() }
        val lastWordIndex = if (lastSpace == -1) 0 else lastSpace + 1
        val lastWord = partial.substring(lastWordIndex)

        if (!lastWord.startsWith(":") || lastWord.endsWith(":") || lastWord.length < 2) return null

        val search = lastWord.lowercase()
        val matches = Emoji.ALL.filter { (trigger, _) ->
            trigger.lowercase().startsWith(search)
        }

        val uniqueMatches = matches.entries
            .groupBy { it.value }
            .mapNotNull { (_, entries) ->
                entries.minByOrNull { it.key.length }
            }

        val range = StringRange.between(lastWordIndex, cursorPosition)
        val suggestionsList = uniqueMatches.map { entry ->
            EmojiSuggestion(range, entry.key, entry.value)
        }

        return CompletableFuture.completedFuture(Suggestions(range, suggestionsList))
    }
}
