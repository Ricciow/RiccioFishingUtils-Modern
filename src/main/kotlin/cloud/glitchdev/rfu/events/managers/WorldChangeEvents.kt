package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object WorldChangeEvents : AbstractEventManager<() -> Unit, WorldChangeEvents.WorldChangeEvent>(), RegisteredEvent {
    private var isMonitoring = false
    private var lastPlayerCount = 0
    private var stableTicks = 0

    private const val REQUIRED_STABLE_TICKS = 15

    override fun register() {
        ClientPlayConnectionEvents.JOIN.register{ _, _, _ ->
            tasks.forEach { if(!it.delayed) it.callback() }

            isMonitoring = true
            lastPlayerCount = 0
            stableTicks = 0
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isMonitoring) return@register

            val playerList = client.networkHandler?.playerList
            val currentCount = playerList?.size ?: 0

            if (currentCount == 0) return@register

            if (currentCount == lastPlayerCount) {
                stableTicks++
            } else {
                stableTicks = 0
                lastPlayerCount = currentCount
            }

            if (stableTicks >= REQUIRED_STABLE_TICKS) {
                isMonitoring = false

                tasks.forEach { if(it.delayed) it.callback() }
            }
        }
    }

    fun registerWorldChangeEvent(priority: Int = 20, delayed: Boolean = false, callback: () -> Unit): WorldChangeEvent {
        return WorldChangeEvent(priority, delayed, callback).register()
    }

    class WorldChangeEvent(
        priority: Int = 20,
        val delayed: Boolean = false,
        callback: () -> Unit,
    ) : ManagedTask<() -> Unit, WorldChangeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}