package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements

/**
 * Manages HUD rendering tasks.
 *
 * Callback Signature: (DrawContext, tickDelta: Float) -> Unit
 */
@AutoRegister
object HudRenderEvents : AbstractEventManager<(DrawContext, Float) -> Unit, HudRenderEvents.HudRenderEvent>(), RegisteredEvent {

    private val HUD_ID = Identifier.of(MOD_ID, "hud_renderer")

    override fun register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            HUD_ID
        ) { context, tick ->
            runTasks(context, tick.getTickProgress(true))
        }
    }

    private fun runTasks(context: DrawContext, tickDelta: Float) {
        for (task in tasks) {
            task.callback(context, tickDelta)
        }
    }

    fun registerHudRenderEvent(priority: Int = 20, callback: (DrawContext, Float) -> Unit): HudRenderEvent {
        return HudRenderEvent(priority, callback).register()
    }

    class HudRenderEvent(
        priority: Int,
        callback: (DrawContext, Float) -> Unit
    ) : ManagedTask<(DrawContext, Float) -> Unit, HudRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}