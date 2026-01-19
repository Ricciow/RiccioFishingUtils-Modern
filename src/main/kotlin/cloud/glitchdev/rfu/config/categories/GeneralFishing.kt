package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.feature.mob.LootshareRange.RARE_SC_REGEX
import cloud.glitchdev.rfu.mixin.ConfigScreenAccessor
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object GeneralFishing : CategoryKt("General Fishing") {
    var lootshareRange by observable(boolean(true) {
        name = Literal("Lootshare Range")
        description = Literal("Shows a sphere around rare sea creatures to display their lootshare range")
    }) { _, _ ->
        val screen = minecraft.currentScreen
        if(screen is ConfigScreenAccessor) {
            screen.invokeClearAndInit()
        }
    }

    var rareSC by observable(draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Rare Sea Creatures")
        description = Literal("Select which sea creatures should display lootshare range")
        condition = { lootshareRange }
    }) { _, new ->
        RARE_SC_REGEX = new.joinToString("|").toRegex()
    }
}