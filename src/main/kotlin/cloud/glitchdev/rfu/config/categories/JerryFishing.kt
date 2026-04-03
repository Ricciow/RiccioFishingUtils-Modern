package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object JerryFishing : Category("Jerry Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for Jerry Fishing!")

    var reindrakeAlert by boolean(true) {
        name = Literal("Reindrake Alert")
        description = Literal("Sends an alert when someone summons a Reindrake!")
    }
}
