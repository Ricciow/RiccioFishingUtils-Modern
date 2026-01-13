package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Command
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen

@RFUFeature
object Settings : Feature {
    override fun onInitialize() {
        Command.registerCommand("rfu") { _ ->
            minecraft.send {
                minecraft.setScreen(ResourcefulConfigScreen.getFactory(MOD_ID).apply(null))
            }
            return@registerCommand 1
        }
    }
}