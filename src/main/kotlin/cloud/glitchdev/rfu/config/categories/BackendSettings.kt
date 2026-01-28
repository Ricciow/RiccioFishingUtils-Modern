package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.utils.network.Network
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object BackendSettings : CategoryKt("Backend Settings") {
    override val description: TranslatableValue
        get() = Literal("Settings for the RFU Backend connection.")

    var backendAccepted by observable(boolean(false) {
        name = Literal("Connect to Backend")
        description = Literal("Allows the mod to connect to the RFU backend for features like authentication.")
    }) { _, newValue ->
        if (newValue) {
            decisionMade = true
            Network.authenticateUser()
        }
    }

    var decisionMade by boolean(false) {
        name = Literal("Decision Made")
        description = Literal("Internal value to track if the user has made a decision.")
        condition = { false }
    }
}
