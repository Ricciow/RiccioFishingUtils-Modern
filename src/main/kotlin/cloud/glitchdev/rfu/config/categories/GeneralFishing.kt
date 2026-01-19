package cloud.glitchdev.rfu.config.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object GeneralFishing : CategoryKt("General Fishing") {
    var lootshareRange by boolean(true) {
        name = Literal("Lootshare Range")
        description = Literal("Shows a sphere around rare sea creatures to display their lootshare range")
    }
}