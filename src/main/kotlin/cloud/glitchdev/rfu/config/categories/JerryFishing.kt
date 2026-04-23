package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object JerryFishing : Category("Jerry Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for Jerry Fishing!")

    init {
        dualSeparator {
            title = "Reindrake"
            description = "Alerts for the Reindrake!"
        }
    }

    var reindrakeAlert by boolean(true) {
        name = Literal("Reindrake Alert")
        description = Literal("Sends an alert when someone summons a Reindrake!")
    }

    init {
        dualSeparator {
            title = "Blizzard"
            description = "Settings for the Jerry's Workshop blizzard!"
        }
    }

    var blizzardTimerDisplay by observable(boolean(true) {
        name = Literal("Blizzard Timer Display")
        description = Literal("Display the current blizzard timer on screen")
    }) { _, _ ->
        reloadScreen()
    }

    var blizzardExpiredAlert by observable(boolean(true) {
        name = Literal("Blizzard Expired Alert")
        description = Literal("Sends an alert whenever the blizzard expires.")
    }) { _, _ ->
        reloadScreen()
    }

    var blizzardExpiredSound by observable(boolean(true) {
        name = Literal("Blizzard Expired Sound")
        description = Literal("Plays a sound whenever the blizzard expires.")
        condition = { blizzardExpiredAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var blizzardExpiredVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the blizzard expired sound")
        range = 0f..1f
        slider = true
        condition = { blizzardExpiredAlert && blizzardExpiredSound }
    }
}
