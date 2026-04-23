package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.MessageTypes
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED

object OtherSettings : Category("Other") {

    init {
        dualSeparator {
            title = "General"
        }
    }

    var emojis by boolean(true) {
        name = Literal("Emojis")
        description = Literal("Replaces triggers like :dog: with the respective high-res emoji.")
    }

    var lobbyTracking by boolean(true) {
        name = Literal("Enable lobby tracking")
        description = Literal("Sends a message whenever you're in a lobby you've been in before.")
    }

    var achievementTrackerDisplay by boolean(true) {
        name = Literal("Achievement Tracker Display")
        description = Literal("Shows the currently tracked achievements.")
    }

    var littlefootAlert by observable(boolean(false) {
        name = Literal("Littlefoot Alert")
        description = Literal("Sends an alert whenever a Littlefoot is found.")
    }) { _, _ ->
        reloadScreen()
    }

    var littlefootSound by observable(boolean(true) {
        name = Literal("Littlefoot Alert Sound")
        description = Literal("Plays a sound whenever a Littlefoot is found.")
        condition = { littlefootAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var littlefootVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the Littlefoot alert sound")
        range = 0f..1f
        slider = true
        condition = { littlefootAlert && littlefootSound }
    }

    var achievementSound by observable(boolean(true) {
        name = Literal("Achievement Sound")
        description = Literal("Plays a sound when you unlock an achievement.")
    }) { _, _ ->
        reloadScreen()
    }

    var achievementVolume by float(1f) {
        name = Literal("Achievement Volume")
        description = Literal("The volume for the achievement unlock sound")
        range = 0f..1f
        slider = true
        condition = { achievementSound }
    }

    var zoomEtherwarp by boolean(false) {
        name = Literal("Zoom on etherwarp")
        description = Literal("Zooms when etherwarping")
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

    var petLevelUpAlert by observable(boolean(true) {
        name = Literal("Pet Level Up Alert")
        description = Literal("Shows an alert on screen when your pet levels up.")
    }) { _, _ ->
        reloadScreen()
    }

    var petLevelUpMinLevel by int(100) {
        name = Literal("Min Level for Alert")
        description = Literal("The minimum level for the alert to trigger.")
        condition = { petLevelUpAlert }
        range = 1..200
        slider = true
    }

    init {
        dualSeparator {
            title = "Party & Alerts"
        }
    }

    init {
        separator {
            title = "Party Finder Alert"
            description = "${LIGHT_RED}This feature requires backend acceptance!"
            condition = { !BackendSettings.backendAccepted }
        }
    }


    var partyFinderAlert by boolean(true) {
        name = Literal("Party Finder Alert")
        description = Literal("Sends a message in chat if there are new parties in the party finder.")
        condition = { BackendSettings.backendAccepted }
    }

    var partyInviteMsgs by boolean(true) {
        name = Literal("Party invite messages")
        description = Literal("Sends a prompt to invite player msg when some keywords are said by player")
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

    var outdatedCake by boolean(true) {
        name = Literal("Outdated cake alert")
        description = Literal("Sends a message when a cake expires")
    }

    init {
        dualSeparator {
            title = "Message Hiding"
        }
    }

    var hideMessages by observable(boolean(false) {
        name = Literal("Enable message hiding")
        description = Literal("Just hides some selected messages")
    }) { _, _ ->
        reloadScreen()
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
}