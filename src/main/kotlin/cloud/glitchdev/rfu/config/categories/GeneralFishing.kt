package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.feature.mob.LootshareRange.RARE_SC_REGEX
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object GeneralFishing : CategoryKt("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

    var rareSC by observable(draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Rare Sea Creatures")
        description = Literal("Select which sea creatures are considered rare for the mod.")
    }) { _, new ->
        RARE_SC_REGEX = new.joinToString("|").toRegex()
    }

    var lootshareRange by boolean(true) {
        name = Literal("Lootshare Range")
        description = Literal("Shows a sphere around rare sea creatures to display their lootshare range")
    }
}