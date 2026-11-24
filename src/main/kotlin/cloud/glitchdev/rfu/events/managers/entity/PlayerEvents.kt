package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.TickEvents.registerTickEvent
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
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Set<Player>) -> Unit
    ): PlayerDetectEventManager.PlayerDetectEvent {
        return PlayerDetectEventManager.register(priority, callback)
    }
}
