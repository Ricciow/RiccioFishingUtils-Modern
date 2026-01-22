package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.Command

@RFUFeature
object Hud : Feature {
    override fun onInitialize() {
        Command.registerCommand("rfumove") { _ ->
            HudWindow.openEditingGui()
            return@registerCommand 1
        }
    }
}