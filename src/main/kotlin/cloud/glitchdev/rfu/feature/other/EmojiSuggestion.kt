package cloud.glitchdev.rfu.feature.other

import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion

class EmojiSuggestion(
    range: StringRange,
    val trigger: String,
    val emoji: String
) : Suggestion(range, trigger) {
    // This weird thing is to prevent it from being replaced by the feature, leading it to having the background of the wrong size
    val displayText: String = "$emoji :§r${trigger.drop(1)
    }"
}
