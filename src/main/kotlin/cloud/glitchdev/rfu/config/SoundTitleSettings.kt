package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.utils.Sounds

open class SoundTitleSettings(
    title: String,
    subtitle: String = "",
    duration: Int = 10,
    fadeIn: Int = 5,
    fadeOut: Int = 5,
    placeholders: String = "None",
    previews: Map<String, List<Pair<String, Any>>> = mapOf("Preview" to emptyList()),
    enabledByDefault: Boolean = true,
    private val soundId: String
) : TitleSettings(
    title = title,
    subtitle = subtitle,
    duration = duration,
    fadeIn = fadeIn,
    fadeOut = fadeOut,
    placeholders = placeholders,
    previews = previews,
    enabledByDefault = enabledByDefault
) {
    init {
        separator {
            this.title = "Sound"
            condition = { enabled }
        }
    }

    var sound by reloadableBoolean(true) {
        name = Literal("Alert Sound")
        description = Literal("Play this alert's sound when it triggers.")
        condition = { enabled }
    }

    var volume by int(100) {
        name = Literal("Alert Sound Volume (%)")
        description = Literal("Volume of this alert's sound.")
        range = 0..500
        condition = { enabled && sound }
    }

    init {
        button {
            this.title = "Preview Sound"
            description = "Play this alert's sound at the configured volume."
            text = "Play"
            condition = { enabled && sound }
            onClick { Sounds.playSound(soundId, volume = volume) }
        }
    }
}
