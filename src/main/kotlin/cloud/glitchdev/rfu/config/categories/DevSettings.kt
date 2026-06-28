package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.utils.network.Network
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object DevSettings : Category("Developer") {
    override val description: TranslatableValue
        get() = Literal("These settings are for developers, don't mess with them if you don't know what you're doing!")

    init {
        dualSeparator {
            title = "General"
            description = "General developer settings"
        }
    }

    var devMode by observable(boolean(false) {
        name = Literal("Developer Mode")
        description = Literal("Enable developer mode.")
    }) { _, _ ->
        reloadScreen()
    }

    var isInSkyblock by boolean(false) {
        name = Literal("Force In Skyblock")
        description = Literal("Forces the mod to consider you're inside skyblock.")
        condition = { devMode }
    }

    init {
        dualSeparator {
            title = "Environment"
            description = "Settings for the mod environment"
        }
    }

    var useCustomBackend by observable(boolean(false) {
        name = Literal("Use Custom Backend")
        description = Literal("Enable to use a custom backend environment URL.")
        condition = { devMode }
    }) { _, _ ->
        reloadScreen()
    }

    var backEndEnvironment by string("http://localhost:8080/api") {
        name = Literal("Back-end Environment")
        description = Literal("The url which the mod will use for its back-end features")
        condition = { devMode && useCustomBackend }
    }

    var bypassHypixelCheck by observable(boolean(false) {
        name = Literal("Bypass Hypixel Check")
        description = Literal("Bypasses the requirement of being on Hypixel to connect to the websocket.")
        condition = { devMode }
    }) { _, _ ->
        Network.authenticateUser()
    }
}