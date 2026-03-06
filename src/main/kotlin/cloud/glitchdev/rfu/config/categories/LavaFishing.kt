package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category

object LavaFishing : Category("Lava Fishing") {
    var jawbus_hard_mode by boolean(false) {
        name = Literal("Jawbus hard mode")
        description = Literal("Pro hint: Don't die")
    }

    var diedJawbusAlert by boolean(true) {
        name = Literal("Jawbus Death Alert")
        description = Literal("Sends an alert whenever someone dies to jawbus")
    }
}