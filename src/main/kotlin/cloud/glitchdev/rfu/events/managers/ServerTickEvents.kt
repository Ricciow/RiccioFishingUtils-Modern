package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.Defaults
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.SetTimeEvents.registerSetTimeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent

@EventManager
object ServerTickEvents : AbstractEventManager<(serverTick: Long) -> Unit, ServerTickEvents.ServerTickEvent>(), RegisteredEvent {
    private const val DEFAULT_INTERVAL = 1L

    var currentServerTick: Long = 0L
        private set

    private var lastGameTime: Long = -1L
    private var targetServerTick: Long = 0L
    private var lastPacketTimeMillis: Long = 0L

    override fun register() {
        registerTickEvent { client ->
            if (client.level == null || lastGameTime == -1L) return@registerTickEvent

            if (currentServerTick < targetServerTick + 20) {
                currentServerTick++
                runTasks(currentServerTick)
            }
        }

        registerSetTimeEvent { gameTime ->
            val now = System.currentTimeMillis()

            if (lastGameTime != -1L && lastPacketTimeMillis != 0L) {
                val serverDelta = gameTime - lastGameTime
                val elapsedSeconds = ((now - lastPacketTimeMillis) / 1000L).coerceAtLeast(1L)
                val maxExpectedTicks = (elapsedSeconds + 1L) * 20L

                if (serverDelta in 1..maxExpectedTicks) {
                    targetServerTick += serverDelta

                    if (currentServerTick < targetServerTick) {
                        currentServerTick = targetServerTick
                        runTasks(currentServerTick)
                    }
                } else {
                    targetServerTick = currentServerTick
                }
            } else {
                targetServerTick = currentServerTick
            }

            lastGameTime = gameTime
            lastPacketTimeMillis = now
        }

        HypixelModApiEvents.registerLocationEvent {
            lastGameTime = -1L
            lastPacketTimeMillis = 0L
        }

        registerDisconnectEvent {
            currentServerTick = 0L
            targetServerTick = 0L
            lastGameTime = -1L
            lastPacketTimeMillis = 0L
        }
    }

    override val runTasks: (Long) -> Unit = { serverTick ->
        safeExecution {
            tasks.forEach { task ->
                if (serverTick % task.interval == 0L) {
                    task.callback(serverTick)
                }
            }
        }
    }

    fun registerServerTickEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        interval: Long = DEFAULT_INTERVAL,
        callback: (serverTick: Long) -> Unit
    ): ServerTickEvent {
        return ServerTickEvent(priority, interval, callback).register()
    }

    @Defaults("interval = ${DEFAULT_INTERVAL}L")
    class ServerTickEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        var interval: Long = DEFAULT_INTERVAL,
        callback: (serverTick: Long) -> Unit
    ) : ManagedTask<(Long) -> Unit, ServerTickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
