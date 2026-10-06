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

    var reindrakeTitleExtraTicks by int(0) {
        name = Literal("Reindrake Title Duration")
        description = Literal("Extra time for the Reindrake title, in ticks.")
        range = 0..200
        slider = true
        condition = { reindrakeAlert }
    }
}
