package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.Emoji

object EmojiFeature {
    /**
     * Replaces ALL registered emoji triggers (e.g., :dog:) with their PUA characters in a String.
     */
    fun replaceEmojis(text: String?): String? {
        if (text == null || !OtherSettings.emojis) return text
        
        var result = text
        Emoji.ALL.forEach { (trigger, replacement) ->
            result = result?.replace(trigger, replacement)
        }
        return result
    }
}
