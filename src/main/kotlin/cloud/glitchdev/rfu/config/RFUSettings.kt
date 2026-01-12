package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.config.dev.DevSettings
import com.teamresourceful.resourcefulconfig.api.annotations.Config

@Config(
    value = "RFU",
    categories = [DevSettings::class]
)
object RFUSettings {

}