package cloud.glitchdev.rfu.data.other

import cloud.glitchdev.rfu.constants.text.EmojiData
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.JsonFile

@RFUFeature
object EmojiPreferences : Feature {
    @Volatile
    private var file: JsonFile<Data>? = null

    override fun onInitialize(scope: EventScope) {
        if (file == null) {
            file = JsonFile(filename = "emoji_preferences.json", type = Data::class.java, defaultFactory = ::Data)
        }
    }

    fun isEnabled(emoji: EmojiData): Boolean = file?.data?.disabled?.contains(emoji.primaryAlias) != true

    fun setEnabled(emoji: EmojiData, enabled: Boolean) {
        val preferences = checkNotNull(file)
        val disabled = preferences.data.disabled
        val updated = if (enabled) disabled - emoji.primaryAlias else disabled + emoji.primaryAlias
        if (updated == disabled) return
        preferences.data.disabled = updated
        preferences.save()
    }

    private class Data {
        @Volatile
        var disabled: Set<String> = emptySet()
    }
}
