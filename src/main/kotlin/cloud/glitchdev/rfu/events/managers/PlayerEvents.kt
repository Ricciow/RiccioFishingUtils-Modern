package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.Tablist
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.player.Player

@AutoRegister
object PlayerEvents : RegisteredEvent {

    override fun register() {
        registerTickEvent(0, 20) { client ->
            val world = client.level ?: return@registerTickEvent
            val user = mc.player ?: return@registerTickEvent

            val players = world.players().filter { it != user && Tablist.getPlayerNames().contains(it.name.toUnformattedString()) }.toSet()
            PlayerDetectEventManager.runTasks(players)
        }
    }

    fun registerPlayerDetectEvent(
        priority: Int = 20,
        callback: (Set<Player>) -> Unit
    ): PlayerDetectEventManager.PlayerDetectEvent {
        return PlayerDetectEventManager.register(priority, callback)
    }

    object PlayerDetectEventManager : AbstractEventManager<(Set<Player>) -> Unit, PlayerDetectEventManager.PlayerDetectEvent>() {
        override val runTasks: (Set<Player>) -> Unit = { players ->
            safeExecution {
                tasks.forEach { task -> task.callback(players) }
            }
        }

        fun register(priority: Int = 20, callback: (Set<Player>) -> Unit): PlayerDetectEvent {
            return PlayerDetectEvent(priority, callback).register()
        }

        class PlayerDetectEvent(
            priority: Int = 20,
            callback: (Set<Player>) -> Unit
        ) : ManagedTask<(Set<Player>) -> Unit, PlayerDetectEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
