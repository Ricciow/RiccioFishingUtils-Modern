package cloud.glitchdev.rfu.events.managers.lifecycle

import cloud.glitchdev.rfu.events.managers.connection.registerLocationEvent
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.connection.registerDisconnectEvent
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.Defaults

@EventManager
object ServerTickEvents : AbstractEventManager<(serverTick: Long) -> Unit, ServerTickEvents.ServerTickEvent>(), RegisteredEvent {
    private const val DEFAULT_INTERVAL = 1L

    var currentServerTick: Long = 0L
        private set

    private var lastGameTime: Long = -1L
    private var targetServerTick: Long = 0L
    private var lastPacketTimeMillis: Long = 0L

    override fun register(scope: EventScope) {
        scope.registerTickEvent { client ->
            if (client.level == null || lastGameTime == -1L) return@registerTickEvent

            if (currentServerTick < targetServerTick + 20) {
                currentServerTick++
                runTasks(currentServerTick)
            }
        }

        scope.registerSetTimeEvent { gameTime ->
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

        scope.registerLocationEvent {
            lastGameTime = -1L
            lastPacketTimeMillis = 0L
        }

        scope.registerDisconnectEvent {
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
