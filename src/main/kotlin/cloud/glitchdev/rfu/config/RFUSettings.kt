package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

object RFUSettings : ConfigKt("RFU") {
    init {
        category(GeneralFishing)
        category(DevSettings)
    }
}