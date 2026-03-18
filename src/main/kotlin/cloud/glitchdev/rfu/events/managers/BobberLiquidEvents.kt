package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import net.minecraft.world.entity.projectile.FishingHook

@AutoRegister
object BobberLiquidEvents : AbstractEventManager<(FishingHook) -> Unit, BobberLiquidEvents.BobberLiquidEvent>(), RegisteredEvent {
    private var wasInLiquid = false

    override fun register() {
        registerTickEvent(0, 5) { _ ->
            val player = mc.player ?: return@registerTickEvent
            val fishingHook = player.fishing ?: run {
                wasInLiquid = false
                return@registerTickEvent
            }

            val isInLiquid = fishingHook.isInWater || fishingHook.isInLava

            if (isInLiquid) {
                if (!wasInLiquid) {
                    wasInLiquid = true
                    runTasks(fishingHook)
                }
            } else {
                wasInLiquid = false
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

    fun registerBobberLiquidEvent(priority: Int = 20, callback: (FishingHook) -> Unit): BobberLiquidEvent {
        return BobberLiquidEvent(priority, callback).register()
    }

    class BobberLiquidEvent(
        priority: Int = 20,
        callback: (FishingHook) -> Unit
    ) : ManagedTask<(FishingHook) -> Unit, BobberLiquidEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
