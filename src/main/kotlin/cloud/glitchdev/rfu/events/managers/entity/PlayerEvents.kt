package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.Tablist
import gg.essential.universal.utils.toUnformattedString

@AutoRegister
object PlayerEvents : RegisteredEvent {

    override fun register(scope: EventScope) {
        scope.registerTickEvent(0, 20) { client ->
            val world = client.level ?: return@registerTickEvent
            val user = mc.player ?: return@registerTickEvent

            val players = world.players().filter { it != user && Tablist.getPlayerNames().contains(it.name.toUnformattedString()) }.toSet()
            PlayerDetectEventManager.runTasks(players)
        }
    }

}
