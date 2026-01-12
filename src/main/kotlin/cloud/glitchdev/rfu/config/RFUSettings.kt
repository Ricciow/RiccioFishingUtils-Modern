package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.config.dev.DevSettings
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

object RFUSettings : ConfigKt("RFU") {
    init {
        category(DevSettings)
    }
}