package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object TrophyFishing : Category("Trophy Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for Trophy Fishing!")

    var trophyPityDisplay by reloadableBoolean(true) {
        name = Literal("Trophy Pity Display")
        description = Literal("Displays trophy pity progress on the screen.")
    }

    var showGoldPity by boolean(false) {
        name = Literal("Show Gold Pity")
        description = Literal("Displays progress towards Gold tier on the pity display.")
        condition = { trophyPityDisplay }
    }

    var showDiamondPity by boolean(true) {
        name = Literal("Show Diamond Pity")
        description = Literal("Displays progress towards Diamond tier on the pity display.")
        condition = { trophyPityDisplay }
    }

    var displayedTrophyFishes by enums(*TrophyFish.entries.toTypedArray()) {
        name = Literal("Displayed Trophy Fishes")
        description = Literal("Select which trophy fishes should be shown on the pity display.")
        condition = { trophyPityDisplay }
    }

    var displayedTrophyFrogs by enums(*TrophyFrog.entries.toTypedArray()) {
        name = Literal("Displayed Trophy Frogs")
        description = Literal("Select which trophy frogs should be shown on the pity display.")
        condition = { trophyPityDisplay }
    }
}
