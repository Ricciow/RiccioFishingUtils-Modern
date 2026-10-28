package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.config.TitleSettings
import cloud.glitchdev.rfu.constants.fishing.InkTrackingType
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object InkFishing : Category("Ink Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for everything with ink fishing!")

    init {
        dualSeparator {
            title = "Tracking"
            description = "Track your ink fishing stats!"
        }
    }

    var inkTrackingDisplay by reloadableBoolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Ink Tracking display")
    }

    var inkTrackingItems by enums(*InkTrackingType.entries.toTypedArray()) {
        name = Literal("Ink Tracking Items")
        description = Literal("Select which items to track in the ink display.")
        condition = { inkTrackingDisplay }
    }

    var fishTrackingOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the display when you're ink fishing")
        condition = { inkTrackingDisplay }
    }

    var goalInk by int(50000) {
        name = Literal("Goal Ink Collection")
        description = Literal("Set an ink collection goal here!")
        condition = {inkTrackingDisplay}
    }

    init {
        separator {
            title = "Notice"
            description = "The pausing behaviour for this is now defined by the settings inside General Fishing"
        }
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Be notified about ink fishing events"
        }
    }

    val noRain by obj(TitleSettings(
        title = "&c&lNO RAIN!",
        subtitle = "&7Go to Vanessa!",
        enabledByDefault = false
    )) {
        name = Literal("No Rain Alert")
        description = Literal("Alerts when fishing without rain in the Park. Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("no rain", "park", "vanessa", "title", "subtitle", "duration")
    }

    val noChumBucket by obj(TitleSettings(
        title = "&c&lNO CHUM BUCKET!",
        enabledByDefault = false
    )) {
        name = Literal("No Chum Bucket Alert")
        description = Literal("Alerts when fishing in the Park without your Chum or Chumcap Bucket. Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("chum", "chumcap", "bucket", "park", "title", "subtitle", "duration")
    }

    val rain by obj(SoundTitleSettings(
        title = "&b&lRain Expired!",
        subtitle = "&7Go to Vanessa!",
        duration = 20,
        fadeIn = 10,
        fadeOut = 10,
        soundId = "rfu:rain_expired"
    )) {
        name = Literal("Rain Expiry Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("rain expiry", "rain", "title", "subtitle", "duration", "sound", "volume", "park", "vanessa", "ink fishing")
    }

}
