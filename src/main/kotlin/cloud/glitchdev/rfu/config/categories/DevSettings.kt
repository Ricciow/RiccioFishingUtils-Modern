package cloud.glitchdev.rfu.config.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object DevSettings : CategoryKt("Developer") {
    var devMode by boolean(false) {
        name = Literal("Developer Mode")
        description = Literal("Enable developer mode.")
    }
}