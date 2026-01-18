package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.config.dev.DevSettings
import cloud.glitchdev.rfu.config.dev.GeneralFishing
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

object RFUSettings : ConfigKt("RFU") {
    init {
        category(GeneralFishing)
        category(DevSettings)
    }
}