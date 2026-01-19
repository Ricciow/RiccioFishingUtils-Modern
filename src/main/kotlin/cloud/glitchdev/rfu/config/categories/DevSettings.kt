package cloud.glitchdev.rfu.config.categories

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object DevSettings : CategoryKt("Developer") {
    override val description: TranslatableValue
        get() = Literal("These settings are for developers, don't mess with them if you don't know what you're doing!")

    var devMode by boolean(false) {
        name = Literal("Developer Mode")
        description = Literal("Enable developer mode.")
    }
}