package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.client.gui.GuiGraphics
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
/*import net.minecraft.resources.ResourceLocation
*///?}
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements

/**
 * Manages HUD rendering tasks.
 *
 * Callback Signature: (DrawContext, tickDelta: Float) -> Unit
 */
@AutoRegister
object HudRenderEvents : AbstractEventManager<(GuiGraphics, Float) -> Unit, HudRenderEvents.HudRenderEvent>(), RegisteredEvent {

    //? if >=1.21.11 {
    private val HUD_ID = Identifier.fromNamespaceAndPath(MOD_ID, "hud_renderer")
    //?} else {
    /*private val HUD_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "hud_renderer")
    *///?}

    override fun register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            HUD_ID
        ) { context, tick ->
            runTasks(context, tick.getGameTimeDeltaPartialTick(true))
        }
    }

    private fun runTasks(context: GuiGraphics, tickDelta: Float) {
        for (task in tasks) {
            task.callback(context, tickDelta)
        }
    }

    fun registerHudRenderEvent(priority: Int = 20, callback: (GuiGraphics, Float) -> Unit): HudRenderEvent {
        return HudRenderEvent(priority, callback).register()
    }

    class HudRenderEvent(
        priority: Int,
        callback: (GuiGraphics, Float) -> Unit
    ) : ManagedTask<(GuiGraphics, Float) -> Unit, HudRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}