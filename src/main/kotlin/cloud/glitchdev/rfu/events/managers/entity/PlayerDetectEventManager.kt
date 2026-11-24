package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.world.entity.player.Player

@EventManager(register = false)
object PlayerDetectEventManager : AbstractEventManager<(Set<Player>) -> Unit, PlayerDetectEventManager.PlayerDetectEvent>() {
    override val runTasks: (Set<Player>) -> Unit = { players ->
        safeExecution {
            tasks.forEach { task -> task.callback(players) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Set<Player>) -> Unit): PlayerDetectEvent {
        return PlayerDetectEvent(priority, callback).register()
    }

    class PlayerDetectEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Set<Player>) -> Unit
    ) : ManagedTask<(Set<Player>) -> Unit, PlayerDetectEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
