package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.FishTrackingType
import cloud.glitchdev.rfu.data.mob.DeployableType
import cloud.glitchdev.rfu.feature.fishing.DoubleHookMessages
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

    var fishingTime by int(5) {
        name = Literal("Fishing Downtime Limit")
        description = Literal("The max ammount of downtime for the trackers to reset in minutes, also used as the window (e.g. 5 -> sc/h during last 5 minutes)")
        range = 0..60
        slider = true
    }

    var pauseSessionOnWindowReached by boolean(false) {
        name = Literal("Pause Session on Downtime")
        description = Literal("Makes the fishing display pause instead of resetting when the downtime limit is reached. You need to use /rfuresetsession or restart the game to reset it.")
    }

    var pauseKeybind by key(0) {
        name = Literal("Pause Keybind")
        description = Literal("Keybind to manually pause the fishing session.")
    }

    var fishTrackingDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Fish Tracking display")
    }) { _, _ ->
        reloadScreen()
    }

    var fishTrackingItems by enums(*FishTrackingType.entries.toTypedArray()) {
        name = Literal("Tracking Items")
        description = Literal("Select which items to track in the display. The display is read like this: Window Rate [Overall Rate] (Total)")
        condition = { fishTrackingDisplay }
    }

    var fishTrackingOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the display when you're fishing")
        condition = { fishTrackingDisplay }
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
        name = Literal("Deployable Expired Alert")
        description = Literal("Toggles the alert for expired deployables")
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
        condition = { deployableExpiredAlert }
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
        name = Literal("Double Hook Messages")
        description = Literal("The messages sent to chat when you get a double hook. It will pick one at random.")
        condition = { toggleDoubleHookMessages }
    }

    init {
        previewButton(
            DoubleHookMessages::preview,
            "Preview Message",
            "Shows a preview of one of the double hook messages in chat."
        ) { toggleDoubleHookMessages }
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

    var failCastSound by observable(boolean(true) {
        name = Literal("Failed Cast Sound")
        description = Literal("Plays a sound whenever a cast fails.")
        condition = { failCastAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var failCastVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the failed cast sound")
        range = 0f..1f
        slider = true
        condition = { failCastAlert && failCastSound }
    }
}
