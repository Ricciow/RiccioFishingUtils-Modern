package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object TrophyFishing : Category("Trophy Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for Trophy Fishing!")

    init {
        dualSeparator {
            title = "Pity"
            description = "Features related to Pity"
        }
    }

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

    var appendPityToTrophyMessage by boolean(true) {
        name = Literal("Append Pity to Trophy Message")
        description = Literal("Appends the count before pity to the trophy catch message in chat (e.g. '(100)').")
    }

    init {
        dualSeparator {
            title = "Trophy Specific"
            description = "Features related to specific trophies"
        }
    }

    var slugfishTimer by boolean(false) {
        name = Literal("Slugfish Timer")
        description = Literal("Displays the 20s timer required to catch slugfish above the bobber, shorter if using slug pet.")
    }
}