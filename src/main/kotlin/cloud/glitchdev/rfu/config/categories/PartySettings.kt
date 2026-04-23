package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object PartySettings : Category("Party") {
    override val description: TranslatableValue
        get() = Literal("Settings for Party related features!")

    init {
        dualSeparator {
            title = "Party Commands"
            description = "Settings for party commands triggered via !command"
        }
    }

    var togglePartyCommands by observable(boolean(true) {
        name = Literal("Toggle Party Commands")
        description = Literal("Enables or disables all party commands triggered via chat.")
    }) { _, _ ->
        reloadScreen()
    }

    var partyCommandPrefix by string("!") {
        name = Literal("Command Prefix")
        description = Literal("The prefix used to trigger party commands (e.g. !help)")
        condition = { togglePartyCommands }
    }

    var spamCooldown by float(3f) {
        name = Literal("Command Cooldown (s)")
        description = Literal("The cooldown period in seconds for party commands. Set to 0 to disable.")
        range = 0f..10f
        slider = true
        condition = { togglePartyCommands }
    }

    init {
        dualSeparator {
            title = "Commands"
            description = "Select which commands are enabled"
            condition = { togglePartyCommands }
        }
    }

    var toggleHelpCommand by boolean(true) {
        name = Literal("Enable Help Command")
        description = Literal("Enables or disables the !help party command.")
        condition = { togglePartyCommands }
    }

    var toggleWarpCommand by boolean(true) {
        name = Literal("Enable Warp Command")
        description = Literal("Enables or disables the !warp party command.")
        condition = { togglePartyCommands }
    }

    var toggleToggleWarpCommand by boolean(true) {
        name = Literal("Enable Togglewarp Command")
        description = Literal("Enables or disables the !togglewarp party command.")
        condition = { togglePartyCommands }
    }
}
