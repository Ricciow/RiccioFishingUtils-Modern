package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object LotusAtollSettings : Category("Lotus Atoll") {
    override val description: TranslatableValue
        get() = Literal("Settings for Lotus Atoll!")

    var lilypadSizeDisplay by boolean(true) {
        name = Literal("Lilypad Size Display")
        description = Literal("Displays a percentage scale above lilypads on Lotus Atoll when they grow/shrink or move.")
    }
}
