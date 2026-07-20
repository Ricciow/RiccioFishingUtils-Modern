package cloud.glitchdev.rfu.feature.other

import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion

class EmojiSuggestion(
    range: StringRange,
    val trigger: String,
    val emoji: String
) : Suggestion(range, trigger) {
    val displayText: String = "$emoji $trigger"
}
