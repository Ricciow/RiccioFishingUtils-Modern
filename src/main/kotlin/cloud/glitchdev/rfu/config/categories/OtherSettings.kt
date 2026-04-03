package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.MessageTypes

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

    var achievementTrackerDisplay by boolean(true) {
        name = Literal("Achievement Tracker Display")
        description = Literal("Shows the currently tracked achievements.")
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

    var partyFinderAlert by boolean(true) {
        name = Literal("Party Finder Alert")
        description = Literal("Sends a message in chat if there are new parties in the party finder.")
    }

    var partyInviteMsgs by boolean(true) {
        name = Literal("Party invite messages")
        description = Literal("Sends a prompt to invite player msg when some keywords are said by player")
    }

    var outdatedCake by boolean(true) {
        name = Literal("Outdated cake alert")
        description = Literal("Sends a message when a cake expires")
    }

    init {
        dualSeparator {
            title = "Tracking"
        }
    }

    var dyeDisplay by boolean(false) {
        name = Literal("Dye Display")
        description = Literal("Shows the currently boosted dyes")
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