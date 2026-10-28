package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.Identifier
import java.awt.Color

object ScreenEffectEvents {
    fun registerWaterOverlayEvent(priority: Int = 20, callback: (OverlayContext) -> Unit): WaterOverlayEventManager.WaterOverlayEvent =
        WaterOverlayEventManager.register(priority, callback)

    fun registerFireOverlayEvent(priority: Int = 20, callback: (OverlayContext) -> Unit): FireOverlayEventManager.FireOverlayEvent =
        FireOverlayEventManager.register(priority, callback)

    fun registerWaterOverlayAppearanceEvent(priority: Int = 20, callback: (WaterOverlayAppearanceContext) -> Unit): WaterOverlayAppearanceEventManager.WaterOverlayAppearanceEvent =
        WaterOverlayAppearanceEventManager.register(priority, callback)

    class OverlayContext(val player: LocalPlayer, var visible: Boolean)

    class WaterOverlayAppearanceContext(val player: LocalPlayer) {
        var color: Color? = null
        var texture: Identifier? = null
    }

    object WaterOverlayAppearanceEventManager : AbstractEventManager<(WaterOverlayAppearanceContext) -> Unit, WaterOverlayAppearanceEventManager.WaterOverlayAppearanceEvent>() {
        override val runTasks: (WaterOverlayAppearanceContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = 20, callback: (WaterOverlayAppearanceContext) -> Unit): WaterOverlayAppearanceEvent =
            WaterOverlayAppearanceEvent(priority, callback).register()

        class WaterOverlayAppearanceEvent(priority: Int, callback: (WaterOverlayAppearanceContext) -> Unit) :
            ManagedTask<(WaterOverlayAppearanceContext) -> Unit, WaterOverlayAppearanceEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object WaterOverlayEventManager : AbstractEventManager<(OverlayContext) -> Unit, WaterOverlayEventManager.WaterOverlayEvent>() {
        override val runTasks: (OverlayContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = 20, callback: (OverlayContext) -> Unit): WaterOverlayEvent =
            WaterOverlayEvent(priority, callback).register()

        class WaterOverlayEvent(priority: Int, callback: (OverlayContext) -> Unit) :
            ManagedTask<(OverlayContext) -> Unit, WaterOverlayEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object FireOverlayEventManager : AbstractEventManager<(OverlayContext) -> Unit, FireOverlayEventManager.FireOverlayEvent>() {
        override val runTasks: (OverlayContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = 20, callback: (OverlayContext) -> Unit): FireOverlayEvent =
            FireOverlayEvent(priority, callback).register()

        class FireOverlayEvent(priority: Int, callback: (OverlayContext) -> Unit) :
            ManagedTask<(OverlayContext) -> Unit, FireOverlayEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}