package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object DevSettings : Category("Developer") {
    override val description: TranslatableValue
        get() = Literal("These settings are for developers, don't mess with them if you don't know what you're doing!")

    var devMode by observable(boolean(false) {
        name = Literal("Developer Mode")
        description = Literal("Enable developer mode.")
    }) { _, _ ->
       reloadScreen()
    }

    var isInSkyblock by boolean(true) {
        name = Literal("Force In Skyblock")
        description = Literal("Forces the mod to consider you're inside skyblock.")
        condition = { devMode }
    }
}