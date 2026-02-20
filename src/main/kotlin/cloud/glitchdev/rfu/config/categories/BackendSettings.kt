package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.utils.network.Network
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object BackendSettings : Category("Backend Settings") {
    override val description: TranslatableValue
        get() = Literal("Settings for the RFU Backend connection.")

    var backendAccepted by observable(boolean(false) {
        name = Literal("Connect to Backend")
        description = Literal("Allows the mod to connect to the RFU backend for features like authentication.")
    }) { _, newValue ->
        reloadScreen()
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

    var shareDyeData by boolean(true) {
        name = Literal("Share Vicent Data")
        description = Literal("Sends the current dyes in rotation to the RFU back-end when the vincent menu is opened so everyone can know them!")
        condition = { backendAccepted }
    }
}
