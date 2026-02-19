package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category

object OtherSettings : Category("Other") {
    var lobbyTracking by boolean(true) {
        name = Literal("Enable lobby tracking")
        description = Literal("Sends a message whenever you're in a lobby you've been in before.")
    }

    var partyFinderAlert by boolean(true) {
        name = Literal("Party Finder Alert")
        description = Literal("Sends a message in chat if there are new parties in the party finder.")
    }
}