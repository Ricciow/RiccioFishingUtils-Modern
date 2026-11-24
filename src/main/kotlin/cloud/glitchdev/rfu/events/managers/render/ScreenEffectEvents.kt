package cloud.glitchdev.rfu.events.managers.render

import java.awt.Color
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.Identifier

object ScreenEffectEvents {
    class OverlayContext(val player: LocalPlayer, var visible: Boolean)

    class WaterOverlayAppearanceContext(val player: LocalPlayer) {
        var color: Color? = null
        var texture: Identifier? = null
    }
}