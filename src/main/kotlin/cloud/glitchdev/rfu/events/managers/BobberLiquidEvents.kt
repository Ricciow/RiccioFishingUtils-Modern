package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import net.minecraft.world.entity.projectile.FishingHook

@EventManager
object BobberLiquidEvents : AbstractEventManager<(FishingHook) -> Unit, BobberLiquidEvents.BobberLiquidEvent>(), RegisteredEvent {
    private var lastHookId = -1

    override fun register() {
        registerTickEvent(0, 5) { _ ->
            val player = mc.player ?: return@registerTickEvent
            val fishingHook = player.fishing ?: run {
                lastHookId = -1
                return@registerTickEvent
            }

            if (fishingHook.id == lastHookId) return@registerTickEvent
            val isInLiquid = fishingHook.isInWater || fishingHook.isInLava

            if (isInLiquid) {
                lastHookId = fishingHook.id
                runTasks(fishingHook)
            }
        }
    }

    override val runTasks: (FishingHook) -> Unit = { bobber ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(bobber)
            }
        }
    }

    fun registerBobberLiquidEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingHook) -> Unit): BobberLiquidEvent {
        return BobberLiquidEvent(priority, callback).register()
    }

    class BobberLiquidEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FishingHook) -> Unit
    ) : ManagedTask<(FishingHook) -> Unit, BobberLiquidEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
