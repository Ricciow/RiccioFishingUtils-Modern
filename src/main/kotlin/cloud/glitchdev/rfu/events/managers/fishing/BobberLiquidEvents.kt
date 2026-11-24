package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.world.entity.projectile.FishingHook

@EventManager
object BobberLiquidEvents : AbstractEventManager<(FishingHook) -> Unit, BobberLiquidEvents.BobberLiquidEvent>(), RegisteredEvent {
    private var lastHookId = -1

    override fun register(scope: EventScope) {
        scope.registerTickEvent(0, 5) { _ ->
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

    class BobberLiquidEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FishingHook) -> Unit
    ) : ManagedTask<(FishingHook) -> Unit, BobberLiquidEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
