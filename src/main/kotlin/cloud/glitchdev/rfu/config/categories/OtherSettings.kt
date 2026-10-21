package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.config.TitleSettings
import cloud.glitchdev.rfu.constants.chat.MessageTypes
import cloud.glitchdev.rfu.constants.ui.TooltipGuiScale
import cloud.glitchdev.rfu.constants.ui.VisiblePlayerEquipment
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED

object OtherSettings : Category("Other") {

    init {
        dualSeparator {
            title = "General"
        }
    }

    var lobbyTracking by boolean(true) {
        name = Literal("Enable lobby tracking")
        description = Literal("Sends a message whenever you're in a lobby you've been in before.")
    }

    var seasonalEffects by boolean(true) {
        name = Literal("Seasonal Effects")
        description = Literal("Shows seasonal decorations and themes.")
    }

    var showActualArrowCount by boolean(true) {
        name = Literal("Show Actual Arrow Count")
        description = Literal("Replaces the selected arrow's stack size with the actual remaining arrow count.")
    }

    var achievementTrackerDisplay by boolean(true) {
        name = Literal("Achievement Tracker Display")
        description = Literal("Shows the currently tracked achievements.")
    }

    var achievementSound by reloadableBoolean(true) {
        name = Literal("Achievement Sound")
        description = Literal("Plays a sound when you unlock an achievement.")
    }

    var achievementVolume by int(100) {
        name = Literal("Achievement Volume")
        description = Literal("The volume for the achievement unlock sound in %.")
        range = 0..500
        condition = { achievementSound }
    }

    val littlefoot by obj(SoundTitleSettings(
        title = "&6&l[&f\uE021&6&l] &3&l{name} &6&l[&f\uE021&6&l]",
        subtitle = "",
        enabledByDefault = false,
        placeholders = "{name}",
        previews = mapOf(
            "Preview" to listOf("name" to "Littlefoot")
        ),
        soundId = "rfu:littlefoot"
    )) {
        name = Literal("Littlefoot Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("littlefoot", "title", "subtitle", "duration", "sound", "volume")
    }

    var zoomEtherwarp by boolean(false) {
        name = Literal("Zoom on etherwarp")
        description = Literal("Zooms when etherwarping")
    }

    var tooltipGuiScale by enum(TooltipGuiScale.DEFAULT) {
        name = Literal("Tooltip GUI Scale")
        description = Literal("The GUI scale for tooltips.")
    }

    var removeNetherFog by boolean(false) {
        name = Literal("Remove Nether Fog")
        description = Literal("Removes the nether fog")
    }

    var partyInviteMsgs by boolean(true) {
        name = Literal("Party invite messages")
        description = Literal("Sends a prompt to invite player msg when some keywords are said by player")
    }

    init {
        dualSeparator {
            title = "Nearby Players"
        }
    }

    var hideNearbyPlayers by reloadableBoolean(false) {
        name = Literal("Hide Nearby Players")
        description = Literal("Hides nearby players armors, nametags and held itens.")
    }

    var hideNearbyPlayersOnlyWhenFishing by boolean(true) {
        name = Literal("Only When Fishing")
        description = Literal("Only hides nearby players while fishing.")
        condition = { hideNearbyPlayers }
    }

    var hideNearbyPlayersRadius by int(5) {
        name = Literal("Hide Players Within")
        description = Literal("Distance in blocks at which other players are hidden.")
        range = 1..10
        slider = true
        condition = { hideNearbyPlayers }
    }

    var visiblePlayerEquipment by enums(VisiblePlayerEquipment.BOOTS) {
        name = Literal("Keep Visible")
        description = Literal("Player parts and equipment to keep visible when nearby players are hidden.")
        condition = { hideNearbyPlayers }
    }

    init {
        dualSeparator {
            title = "Global Player Settings"
        }
    }

    var hidePlayersGlobally by reloadableBoolean(false) {
        name = Literal("Hide Global Player Armors")
        description = Literal("Hides armors at any distance, including your own.")
    }

    var globallyVisiblePlayerEquipment by enums(
        VisiblePlayerEquipment.NAME_TAG,
        VisiblePlayerEquipment.PLAYER_MODEL,
        VisiblePlayerEquipment.HELD_ITEMS
    ) {
        name = Literal("Keep Visible")
        description = Literal("Select what to keep visible.")
        condition = { hidePlayersGlobally }
    }

    init {
        dualSeparator {
            title = "Emojis"
        }
    }

    var emojis by reloadableBoolean(true) {
        name = Literal("Toggle Emojis")
        description = Literal("Replaces triggers like :dog: with the respective emoji. Use /rfuemojis to see all emojis!")
    }

    var returnCompletesEmoji by boolean(true) {
        name = Literal("Return Completes Emoji")
        description = Literal("Pressing Return completes the emoji suggestion instead of sending the message.")
        condition = { emojis }
    }

    var requiredChars by int(2) {
        name = Literal("Matching Chars Requirement")
        description = Literal("Changes the minimum amount of matching chars required to display the suggestions")
        range = 1..3
        condition = { emojis }
        slider = true
    }

    init {
        dualSeparator {
            title = "Pets"
        }
    }

    var petDisplay by boolean(true) {
        name = Literal("Pet Display")
        description = Literal("Shows the currently equipped pet.")
    }

    val petLevelUp by obj(PetLevelTitleSettings()) {
        name = Literal("Pet Level Up Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("pet level up", "petlevelup", "minimum level", "min level", "title", "subtitle", "duration")
    }

    init {
        dualSeparator {
            title = "Party & Alerts"
        }
    }

    val fullInventory by obj(SoundTitleSettings(
        title = "&c&lINVENTORY FULL!",
        duration = 20,
        enabledByDefault = false,
        soundId = "rfu:inventory_full"
    )) {
        name = Literal("Full Inventory Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("full inventory", "fullinventory", "title", "subtitle", "duration", "sound", "volume")
    }

    init {
        separator {
            title = "Party Finder"
            description = "${LIGHT_RED}This feature requires backend acceptance!"
            condition = { !BackendSettings.backendAccepted }
        }
    }

    var partyFinderAlert by boolean(false) {
        name = Literal("Party Finder Alert")
        description = Literal("Sends a message in chat if there are new parties in the party finder.")
        condition = { BackendSettings.backendAccepted }
    }

    var partyRequeueAlert by boolean(true) {
        name = Literal("Party Requeue Alert")
        description = Literal("Sends a message with a requeue button when your party is dequeued or stops being full.")
        condition = { BackendSettings.backendAccepted }
    }

    var autoAcceptPlayers by boolean(false) {
        name = Literal("Auto accept players")
        description = Literal("Automatically invites players who request to join your Party Finder listing.")
        condition = { BackendSettings.backendAccepted }
    }

    var peekPartyFinderKeybind by key(0) {
        name = Literal("Peek Party Finder Keybind")
        description = Literal("Keybind to peek the party finder window without opening the GUI.")
        condition = { BackendSettings.backendAccepted }
    }

    init {
        dualSeparator {
            title = "Tracking"
        }

        separator {
            title = "Dye Display"
            description = "${LIGHT_RED}This feature requires backend acceptance!"
            condition = { !BackendSettings.backendAccepted }
        }
    }

    var dyeDisplay by boolean(false) {
        name = Literal("Dye Display")
        description = Literal("Shows the currently boosted dyes")
        condition = { BackendSettings.backendAccepted }
    }

    var bestiaryDisplay by boolean(true) {
        name = Literal("Bestiary Display")
        description = Literal("Shows the bestiary progress from the tablist. Requires the Bestiary Tablist to work.")
    }

    var tpsDisplay by boolean(false) {
        name = Literal("TPS Display")
        description = Literal("Displays the server's current TPS.")
    }

    var outdatedCake by boolean(true) {
        name = Literal("Outdated cake alert")
        description = Literal("Sends a message when a cake expires")
    }

    init {
        dualSeparator {
            title = "Message Hiding"
        }
    }

    var hideMessages by reloadableBoolean(false) {
        name = Literal("Enable message hiding")
        description = Literal("Just hides some selected messages")
    }

    var hiddenMessageTypes by enums(*MessageTypes.entries.toTypedArray()) {
        name = Literal("Message types")
        description = Literal("The types of messages that will be hidden")
        condition = { hideMessages }
    }

    var walkthroughAcknowledged by boolean(false) {
        name = Literal("Walkthrough Acknowledged")
        description = Literal("Whether the user has acknowledged the mod's walkthrough.")
        condition = { false }
    }

    init {
        dualSeparator {
            title = "Resource Packs"
        }
    }

    var autoAcceptResourcePacks by reloadableBoolean(false) {
        name = Literal("Auto Accept Resource Packs")
        description = Literal("Automatically accept resource/texture packs sent by the server if you are on Hypixel.")
    }

    var saveResourcePacks by boolean(true) {
        name = Literal("Save for future use")
        description = Literal("Saves server-sent resource packs to your local resource packs folder to load them on startup and avoid reloads.")
        condition = { autoAcceptResourcePacks }
    }

    var autoLoadResourcePacks by boolean(true) {
        name = Literal("Auto Load Resource Packs")
        description = Literal("Automatically loads the server-sent resource packs. If disabled, packs are still accepted and saved but not loaded into the game.")
        condition = { autoAcceptResourcePacks }
    }

    var deleteOldResourcePacks by boolean(true) {
        name = Literal("Delete Old Resource Packs")
        description = Literal("Deletes older versions of server resource packs when their hash changes to save disk space.")
        condition = { autoAcceptResourcePacks && saveResourcePacks }
    }

    init {
        dualSeparator {
            title = "Fixes & Performance"
        }
    }

    var patchElementaMemoryLeaks by boolean(true) {
        name = Literal("Patch Elementa Memory Leaks")
        description = Literal("Patches memory leaks in Elementa")
    }

    var patchSkyOceanTextWidth by boolean(true) {
        name = Literal("Patch SkyOcean Text Width")
        description = Literal("Prevents text from being cut off in RFU HUDs, windows and chat suggestions when SkyOcean Text Replacements is enabled.")
    }

    var fixFailedCasts by boolean(false) {
        name = Literal("Fix failed casts")
        description = Literal("Fixes failed casts caused by duplicate interaction packets when holding a fishing rod.\n${LIGHT_RED}Use at your own risk!")
    }

    class PetLevelTitleSettings : TitleSettings(
        title = "&a&lPET LEVEL UP!",
        subtitle = "&eYour &6{pet} &eleveled up to level &6{level}&e!",
        duration = 40,
        fadeIn = 10,
        fadeOut = 10,
        placeholders = "{pet}, {level}",
        previews = mapOf(
            "Preview" to listOf("pet" to "Golden Dragon", "level" to "100")
        )
    ) {
        init {
            separator {
                title = "Trigger Options"
                condition = { enabled }
            }
        }

        var minimumLevel by int(100) {
            name = Literal("Min Level for Alert")
            description = Literal("The minimum level for the alert to trigger.")
            range = 1..200
            slider = true
            condition = { enabled }
        }
    }
}
