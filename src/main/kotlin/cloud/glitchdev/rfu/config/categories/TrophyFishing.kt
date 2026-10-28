package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.fishing.SulphurSkitterRange.SulphurSkitterMode
import cloud.glitchdev.rfu.feature.fishing.TrophyCatchMessageReplacer
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

    var displayRecentTrophies by boolean(true) {
        name = Literal("Display only recently caught trophies")
        description = Literal("Makes the pity display only display recently caught trophies")
        condition = { trophyPityDisplay }
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

    init {
        dualSeparator {
            title = "Trophy Messages"
        }
    }

    var customTrophyMessageEnabled by reloadableBoolean(true) {
        name = Literal("Custom Trophy Message")
        description = Literal("Replaces trophy frog and fish catch messages with a custom message.")
    }

    var customTrophyMessage by string("&2&l\uE02A TROPHY {type}! &fYou caught your &7{count}{ordinal} {color}{name} {tier_color}&l{tier}{multi}&f! {pity}") {
        name = Literal("Custom Trophy Message Format")
        description = Literal("Variables: {type}, {name}, {color}, {tier}, {count}, {ordinal}, {amount}, {multi}, {pity}.")
        condition = { customTrophyMessageEnabled }
    }

    var customTrophyPityCount by string("&7({pity}&7)") {
        name = Literal("Custom Trophy Pity Count")
        description = Literal("Gold and Diamond pity suffix. Variables: {pity}")
        condition = { customTrophyMessageEnabled }
    }

    init {
        previewButton(
            TrophyCatchMessageReplacer::preview,
            "Preview Trophy Message",
            "Shows a preview of the custom trophy catch message in chat."
        ) { customTrophyMessageEnabled }
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

    var sulphurSkitterRange by reloadableBoolean(true) {
        name = Literal("Sulphur Skitter Range")
        description = Literal("Shows the four block range around sulphur blocks while trophy fishing.")
    }

    var sulphurSkitterMode by enum(SulphurSkitterMode.MANUAL) {
        name = Literal("Sulphur Skitter Range Mode")
        description = Literal("Auto highlights sulphur blocks within 30 blocks. Manual lets you right-click a sponge to toggle its range.")
        condition = { sulphurSkitterRange }
    }

    init {
        dualSeparator {
            title = "Trophy Sacks"
            description = "Features related to Trophy Fishing and Frog Sacks"
        }
    }

    var trophySackDisplay by reloadableBoolean(true) {
        name = Literal("Trophy Sack Value Display")
        description = Literal("Displays fillet (Magmafish) and donation (Lotuses) rewards when opening Trophy Sacks.")
    }

    var trophySackShowBreakdown by boolean(true) {
        name = Literal("Show Trophy Breakdown")
        description = Literal("Displays each individual trophy stored in the sack alongside its reward.")
        condition = { trophySackDisplay }
    }

    var trophySackShowEmpty by boolean(false) {
        name = Literal("Show Empty Trophies")
        description = Literal("Shows trophies with 0 stored count in the breakdown.")
        condition = { trophySackDisplay && trophySackShowBreakdown }
    }
}
