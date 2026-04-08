package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.getResource
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor' {
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Manages HUD rendering tasks.
 *
 * Callback Signature: (DrawContext, tickDelta: Float) -> Unit
 */
@AutoRegister
object HudRenderEvents : AbstractEventManager<(GuiGraphicsExtractor, Float) -> Unit, HudRenderEvents.HudRenderEvent>(), RegisteredEvent {
    private val HUD_ID = getResource("hud_renderer")

    override fun register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            HUD_ID
        ) { context, tick ->
            runTasks(context, tick.getGameTimeDeltaPartialTick(true))
        }
    }

    override val runTasks: (GuiGraphicsExtractor, Float) -> Unit = { context, tickDelta ->
        safeExecution {
            for (task in tasks) {
                task.callback(context, tickDelta)
            }
        }
    }

    fun registerHudRenderEvent(priority: Int = 20, callback: (GuiGraphicsExtractor, Float) -> Unit): HudRenderEvent {
        return HudRenderEvent(priority, callback).register()
    }

    class HudRenderEvent(
        priority: Int,
        callback: (GuiGraphicsExtractor, Float) -> Unit
    ) : ManagedTask<(GuiGraphicsExtractor, Float) -> Unit, HudRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
//~}