package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

object RFUSettings : ConfigKt("rfu/settings") {
    override val name: TranslatableValue
        get() = Literal("RiccioFishingUtils")
    override val description: TranslatableValue
        get() = Literal("Settings for the greatest hit mod RFU")

    init {
        category(GeneralFishing)
        category(BackendSettings)
        category(DevSettings)
    }
}