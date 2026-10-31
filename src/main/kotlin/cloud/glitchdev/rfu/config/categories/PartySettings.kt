package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.chat.PartyCommandListMode
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

    var togglePartyCommands by reloadableBoolean(true) {
        name = Literal("Toggle Party Commands")
        description = Literal("Enables or disables all party commands triggered via chat.")
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
            title = "Party Command Access"
            description = "Choose who can trigger party commands"
            condition = { togglePartyCommands }
        }
    }

    var partyCommandListMode by enum(PartyCommandListMode.BLACKLIST) {
        name = Literal("List Mode")
        description = Literal("Blacklist blocks listed players. Whitelist only allows listed players.")
        condition = { togglePartyCommands }
    }

    val partyCommandPlayers by list(listOf<String>()) {
        name = Literal("Player Names")
        description = Literal("Player usernames to blacklist or whitelist. Case insensitive.")
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

    var toggleSinceCommand by boolean(true) {
        name = Literal("Enable Since Command")
        description = Literal("Enables or disables the !since party command.")
        condition = { togglePartyCommands }
    }

    var toggleStreakCommand by boolean(true) {
        name = Literal("Enable Streak Command")
        description = Literal("Enables or disables the !streak party command.")
        condition = { togglePartyCommands }
    }

    var toggleAllInviteCommand by boolean(true) {
        name = Literal("Enable AllInvite Command")
        description = Literal("Enables or disables the !allinv party command.")
        condition = { togglePartyCommands }
    }

    var toggleTransferCommand by boolean(true) {
        name = Literal("Enable Transfer Command")
        description = Literal("Enables or disables the !pt party command.")
        condition = { togglePartyCommands }
    }

    var toggleCoordsCommand by boolean(true) {
        name = Literal("Enable Coords Command")
        description = Literal("Enables or disables the !coords party command.")
        condition = { togglePartyCommands }
    }

    var toggleSchCommand by boolean(true) {
        name = Literal("Enable Sch Command")
        description = Literal("Enables or disables the !sch party command.")
        condition = { togglePartyCommands }
    }

    var toggleXphCommand by boolean(true) {
        name = Literal("Enable Xph Command")
        description = Literal("Enables or disables the !xph party command.")
        condition = { togglePartyCommands }
    }

    var toggleScCommand by boolean(true) {
        name = Literal("Enable SC Command")
        description = Literal("Enables or disables the !sc party command.")
        condition = { togglePartyCommands }
    }

    var togglePbCommand by boolean(true) {
        name = Literal("Enable PB Command")
        description = Literal("Enables or disables the !pb party command.")
        condition = { togglePartyCommands }
    }

    var toggleDropCommand by boolean(true) {
        name = Literal("Enable Drop Command")
        description = Literal("Enables or disables the !drop party command.")
        condition = { togglePartyCommands }
    }
}
