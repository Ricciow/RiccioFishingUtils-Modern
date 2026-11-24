package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import java.awt.Color
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.Identifier

object ScreenEffectEvents {
    fun registerWaterOverlayEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (OverlayContext) -> Unit): WaterOverlayEventManager.WaterOverlayEvent =
        WaterOverlayEventManager.register(priority, callback)

    fun registerFireOverlayEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (OverlayContext) -> Unit): FireOverlayEventManager.FireOverlayEvent =
        FireOverlayEventManager.register(priority, callback)

    fun registerWaterOverlayAppearanceEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (WaterOverlayAppearanceContext) -> Unit): WaterOverlayAppearanceEventManager.WaterOverlayAppearanceEvent =
        WaterOverlayAppearanceEventManager.register(priority, callback)

    class OverlayContext(val player: LocalPlayer, var visible: Boolean)

    class WaterOverlayAppearanceContext(val player: LocalPlayer) {
        var color: Color? = null
        var texture: Identifier? = null
    }
}