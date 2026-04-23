package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category

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

    var diedJawbusSound by observable(boolean(true) {
        name = Literal("Jawbus Death Sound")
        description = Literal("Plays a sound whenever someone dies to jawbus")
        condition = { diedJawbusAlert }
    }) { _, _ ->
        reloadScreen()
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
}