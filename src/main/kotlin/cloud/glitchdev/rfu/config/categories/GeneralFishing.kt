package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.categories.RareScSettings.detectionAlert
import cloud.glitchdev.rfu.constants.FishTrackingType
import cloud.glitchdev.rfu.data.mob.DeployableType
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object GeneralFishing : Category("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

    init {
        dualSeparator {
            title = "Fish Tracking"
            description = "Track your fishing stats!"
        }
    }

    var fishTrackingDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Fish Tracking display")
    }) { _, _ ->
        reloadScreen()
    }

    var fishTrackingItems by enums(*FishTrackingType.entries.toTypedArray()) {
        name = Literal("Tracking Items")
        description = Literal("Select which items to track in the display.")
        condition = { fishTrackingDisplay }
    }

    var fishTrackingOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the display when you're fishing")
        condition = { fishTrackingDisplay }
    }

    var fishingTime by int(5) {
        name = Literal("Fishing Downtime Limit")
        description = Literal("The max ammount of downtime for the trackers to reset in minutes, also used as the window (e.g. 5 -> sc/h during last 5 minutes)")
        condition = { fishTrackingDisplay }
        range = 0..60
        slider = true
    }

    init {
        dualSeparator {
            title = "Deployables"
            description = "Everything deployables related, Flares, Fluxes, You name it!"
        }
    }

    var deployableDisplay by observable(boolean(true) {
        name = Literal("Deployable Display")
        description = Literal("Toggles the deployable display")
    }) { _, _ ->
        reloadScreen()
    }

    var deployableTimerDisplay by enums(*DeployableType.entries.toTypedArray()) {
        name = Literal("Deployable Timers")
        description = Literal("Select which deployable timers to display.")
        condition = { deployableDisplay }
    }

    var deployableExpiredAlert by observable(boolean(true) {
        name = Literal("Deployable Display")
        description = Literal("Toggles the deployable display")
    }) { _, _ ->
        reloadScreen()
    }

    var deployableAlertTypes by enums(*DeployableType.entries.toTypedArray()) {
        name = Literal("Deployable Alerts")
        description = Literal("Select which deployable will cause an alert.")
        condition = { deployableDisplay }
    }

    var deployableExpiredSound by observable(boolean(true) {
        name = Literal("Expired Sound")
        description = Literal("Plays a sound whenever a deployable expires.")
        condition = { detectionAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var deployableExpiredVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the expired sound")
        range = 0f..1f
        slider = true
        condition = { deployableExpiredAlert && deployableExpiredSound }
    }

    init {
        dualSeparator {
            title = "Double Hook"
            description = "Double hook shenanigans"
        }
    }

    var toggleDoubleHookMessages by observable(boolean(false) {
        name = Literal("Toggle Double Hook Messages")
        description = Literal("Automatically send messages when you get a double hook!")
    }) { _, _ ->
        reloadScreen()
    }

    var doubleHookMessages by strings(
        "o/ &9~~~~~~~|&f_&9|",
        "o| &9~~~~~~~&c.&9~",
        "o| &9~~~~~~~&c*&9~",
        "o| &9~~~~~~&3<><",
        "o| &9~~~~&3<><&9~~",
        "o| &9~~&3<><&9~~~~",
        "\\o/ &3<><&9~~~~~",
        "( ^_^) &b[ &3<>< &b]",
        "( >_<) &b[ &3RFU &b]"
    ) {
        name = Literal("Double Hook messages")
        description = Literal("Select what words will be sent when you get a double hook. Each line is one phrase.")
        condition = { toggleDoubleHookMessages }
    }

    var randomDoubleHookMessages by boolean(false) {
        name = Literal("Random Double Hook Messages")
        description = Literal("Makes double hook messages random")
        condition = { toggleDoubleHookMessages }
    }

    init {
        dualSeparator {
            title = "Fishing"
            description = "Anything fishing related that didn't fit elsewhere"
        }
    }

    var rodTimerDisplay by boolean(false) {
        name = Literal("Rod Timer Display")
        description = Literal("Display the current rod timer on screen")
    }

    var failCastAlert by boolean(true) {
        name = Literal("Failed cast alert")
        description = Literal("Sends an alert whenever a rod cast fails.")
    }

    var failCastSound by observable(boolean(false) {
        name = Literal("Failed Cast Sound")
        description = Literal("Plays a sound whenever a cast fails.")
        condition = { failCastAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var failCastVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the expired sound")
        range = 0f..1f
        slider = true
        condition = { failCastAlert && failCastSound }
    }
}
