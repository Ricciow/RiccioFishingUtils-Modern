package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.InkTrackingType
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

    var inkTrackingDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Ink Tracking display")
    }) { _, _ ->
        reloadScreen()
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

    var fishingTimeAFK by int(5) {
        name = Literal("Fishing Downtime Limit (Inking)")
        description = Literal("The max ammount of downtime for the trackers to pause in minutes")
        condition = { inkTrackingDisplay }
        range = 0..30
        slider = true
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Be notified about ink fishing events"
        }
    }

    var rainAlert by observable(boolean(true) {
        name = Literal("Rain Alert")
        description = Literal("Show an alert when rain expires in the park")
    }) { _, _ ->
        reloadScreen()
    }

    var rainAlertSound by observable(boolean(true) {
        name = Literal("Rain Alert Sound")
        description = Literal("Plays a sound when rain expires in the park")
        condition = { rainAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var rainAlertVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the rain alert sound")
        range = 0f..1f
        slider = true
        condition = { rainAlert && rainAlertSound }
    }
}