package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.feature.fishing.VanquisherPartyMessage

object LavaFishing : Category("Lava Fishing") {
    init {
        dualSeparator {
            title = "Jawbus"
            description = "Settings for the final boss."
        }
    }

    var jawbus_hard_mode by boolean(false) {
        name = Literal("Jawbus hard mode")
        description = Literal("Pro hint: Don't die")
    }

    var diedJawbusAlert by boolean(true) {
        name = Literal("Jawbus Death Alert")
        description = Literal("Sends an alert whenever someone dies to jawbus")
    }

    var diedJawbusSound by reloadableBoolean(true) {
        name = Literal("Jawbus Death Sound")
        description = Literal("Plays a sound whenever someone dies to jawbus")
        condition = { diedJawbusAlert }
    }

    var diedJawbusVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the jawbus death sound")
        range = 0f..1f
        slider = true
        condition = { diedJawbusAlert && diedJawbusSound }
    }

    init {
        dualSeparator {
            title = "Plhlegblast"
            description = "Settings for the ellusive Plhlegblast"
        }
    }

    var plhlegblastGlow by boolean(true) {
        name = Literal("Plhlegblast Glow")
        description = Literal("Visually transforms Squids named 'Plhlegblast' into Glow Squids")
    }

    init {
        dualSeparator {
            title = "Vanquishers"
            description = "Settings for Vanquishers"
        }
    }

    var vanquisherPartyMessages by reloadableBoolean(true) {
        name = Literal("Vanquisher Party Message")
        description = Literal("Sends a party message when a Vanquisher spawns.")
    }

    var vanquisherPartyMessageNoFishing by string("A Vanquisher has spawned! {coords}") {
        name = Literal("Vanquisher Message (Not Fishing)")
        description = Literal("The message sent when a Vanquisher spawns and you are not fishing. Params: {count}, {coords}")
        condition = { vanquisherPartyMessages }
    }

    var vanquisherPartyMessageFishing by string("A Vanquisher has spawned! #{count} SCs") {
        name = Literal("Vanquisher Message (Fishing)")
        description = Literal("The message sent when a Vanquisher spawns and you are fishing. Params: {count}, {coords}")
        condition = { vanquisherPartyMessages }
    }

    init {
        previewButton(
            VanquisherPartyMessage::previewNoFishing,
            "Preview Message (Not Fishing)",
            "Shows a preview of the vanquisher party message when not fishing in chat."
        ) { vanquisherPartyMessages }

        previewButton(
            VanquisherPartyMessage::previewFishing,
            "Preview Message (Fishing)",
            "Shows a preview of the vanquisher party message when fishing in chat."
        ) { vanquisherPartyMessages }
    }
}