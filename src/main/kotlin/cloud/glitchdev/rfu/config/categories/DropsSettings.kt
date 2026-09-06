package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.fishing.RareDropDisplayDataType
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.feature.drops.RareDropAlert
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object DropsSettings : Category("Drops") {
    override val description: TranslatableValue
        get() = Literal("Settings for your fishing drops!")

    init {
        dualSeparator {
            title = "Drop Selection"
            description = "Select which drops are tracked"
        }
    }

    var rareDrops by draggable(*RareDrops.entries.toTypedArray()) {
        name = Literal("Rare Drops")
        description = Literal("Select which drops are considered rare for the mod.")
    }

    var dyeDrops by draggable(*Dyes.entries.toTypedArray()) {
        name = Literal("Dye drops")
        description = Literal("Select which dyes are considered rare for the mod.")
    }

    init {
        dualSeparator {
            title = "Chat Messages"
            description = "Customize the messages sent to chat"
        }
    }

    var customRareDropMessage by reloadableBoolean(true) {
        name = Literal("Enable Custom Rare Drop Message")
        description = Literal("Shows a custom message when you get a rare drop")
    }

    var rareDropMessageFormat by string("&6&lRARE DROP! &e{drop} &b(+{magic_find} \uE01A Magic Find) &7(Took {count} catches, {time} since last)") {
        name = Literal("Custom Message Format")
        description = Literal("Variables: {drop}, {dropcolor}, {mob}, {magic_find}, {count}, {time}, {total}")
        condition = { customRareDropMessage }
    }

    init {
        previewButton(
            RareDropAlert::previewMessage,
            "Preview Message",
            "Shows a preview of the rare drop message in chat."
        ) { customRareDropMessage }
    }

    var rareDropPartyChat by boolean(true) {
        name = Literal("Send in party chat")
        description = Literal("Sends the drop message in party chat, uses the same message as above but removes the colors")
        condition = { customRareDropMessage }
    }

    var lootshareMessage by boolean(true) {
        name = Literal("Lootshare Message")
        description = Literal("Sends a message when lootshare gives you an item.")
    }

    init {
        dualSeparator {
            title = "On-Screen Alerts"
            description = "Configure the rare drop titles and subtitles"
        }
    }

    var rareDropTitleAlert by reloadableBoolean(true) {
        name = Literal("Rare Drop Title Alert")
        description = Literal("Shows a title on screen when you get a rare drop")
    }

    var rareDropTitleFormat by string("{dropcolor}&l{drop}") {
        name = Literal("Rare Drop Title Format")
        description = Literal("The title to show on screen. Variables: {drop}, {dropcolor}, {mob}, {magic_find}, {count}, {time}, {total}")
        condition = { rareDropTitleAlert }
    }

    var rareDropSubtitleFormat by string("&b(+{magic_find} \uE01A Magic Find)") {
        name = Literal("Rare Drop Subtitle Format")
        description = Literal("The subtitle to show on screen. Variables: {drop}, {dropcolor}, {mob}, {magic_find}, {count}, {time}, {total}")
        condition = { rareDropTitleAlert }
    }

    init {
        previewButton(
            RareDropAlert::previewTitle,
            "Preview Title",
            "Shows a preview of the rare drop title on screen."
        ) { rareDropTitleAlert }
    }

    init {
        dualSeparator {
            title = "Rare Drops Display"
            description = "Track your rare drops!"
        }
    }

    var rareDropsDisplay by reloadableBoolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Rare Drops display")
    }

    var rareDropsOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the rare drops when fishing")
        condition = { rareDropsDisplay }
    }

    var rareDropsDisplayDyes by boolean(true) {
        name = Literal("Include Dyes")
        description = Literal("Include Dyes in the display")
        condition = { rareDropsDisplay }
    }

    var displayedRareDrops by enums(*RareDrops.entries.toTypedArray()) {
        name = Literal("Displayed Rare Drops")
        description = Literal("Select which rare drops will appear on the display")
        condition = { rareDropsDisplay }
    }

    var rareDropsDisplayDataOrder by draggable(*RareDropDisplayDataType.entries.toTypedArray()) {
        name = Literal("Display Data Order")
        description = Literal("Drag to reorder the data shown for each drop.")
        condition = { rareDropsDisplay }
    }
}
