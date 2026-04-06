package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
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

    var customRareDropMessage by observable(boolean(false) {
        name = Literal("Enable Custom Rare Drop Message")
        description = Literal("Shows a custom message when you get a rare drop")
    }) { _, _ ->
        reloadScreen()
    }

    var rareDropMessageFormat by string("&6&lRARE DROP! &e{drop} &b(+{magic_find} ✯ Magic Find) &7(Took {count} catches, {time} since last)") {
        name = Literal("Custom Message Format")
        description = Literal("Variables: {drop}, {magic_find}, {count}, {time}, {total}")
        condition = { customRareDropMessage }
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

    var rareDropTitleAlert by observable(boolean(true) {
        name = Literal("Rare Drop Title Alert")
        description = Literal("Shows a title on screen when you get a rare drop")
    }) { _, _ ->
        reloadScreen()
    }

    var rareDropTitleFormat by string("{dropcolor}&l{drop}") {
        name = Literal("Rare Drop Title Format")
        description = Literal("The title to show on screen. Variables: {drop}, {dropcolor}, {magic_find}, {count}, {time}, {total}")
        condition = { rareDropTitleAlert }
    }

    var rareDropSubtitleFormat by string("&b(+{magic_find} ✯ Magic Find)") {
        name = Literal("Rare Drop Subtitle Format")
        description = Literal("The subtitle to show on screen. Variables: {drop}, {dropcolor}, {magic_find}, {count}, {time}, {total}")
        condition = { rareDropTitleAlert }
    }
}
