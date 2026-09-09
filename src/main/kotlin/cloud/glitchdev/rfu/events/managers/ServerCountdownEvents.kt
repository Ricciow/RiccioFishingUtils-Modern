package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ServerTickEvents.registerServerTickEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@AutoRegister
object ServerCountdownEvents : AbstractEventManager<() -> Unit, ServerCountdownEvents.ServerCountdownEvent>(), RegisteredEvent {
    override fun register() {
        registerServerTickEvent(0) {
            runTasks()
        }
    }

    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task ->
                task.tick()
            }
        }
    }

    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long = 1L,
        priority: Int = 20,
        onTick: ((event: ServerCountdownEvent) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ): ServerCountdownEvent {
        return ServerCountdownEvent(priority, durationTicks, interval, onTick, onComplete).register()
    }

    fun registerServerCountdownEvent(
        durationTicks: Long,
        priority: Int = 20,
        onComplete: () -> Unit
    ): ServerCountdownEvent {
        return registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = 1L,
            priority = priority,
            onTick = null,
            onComplete = onComplete
        )
    }

    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long,
        priority: Int = 20,
        onTick: (event: ServerCountdownEvent) -> Unit
    ): ServerCountdownEvent {
        return registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = interval,
            priority = priority,
            onTick = onTick,
            onComplete = null
        )
    }

    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long,
        onTick: (event: ServerCountdownEvent) -> Unit,
        onComplete: () -> Unit
    ): ServerCountdownEvent {
        return registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = interval,
            priority = 20,
            onTick = onTick,
            onComplete = onComplete
        )
    }

    class ServerCountdownEvent(
        priority: Int = 20,
        val durationTicks: Long,
        var interval: Long = 1L,
        val onTick: ((event: ServerCountdownEvent) -> Unit)? = null,
        val onComplete: (() -> Unit)? = null
    ) : ManagedTask<() -> Unit, ServerCountdownEvent>(priority, onComplete ?: {}) {

        val startServerTick: Long = ServerTickEvents.currentServerTick
        var targetServerTick: Long = startServerTick + durationTicks
            private set

        val remainingTicks: Long
            get() = (targetServerTick - ServerTickEvents.currentServerTick).coerceAtLeast(0L)

        val elapsedTicks: Long
            get() = durationTicks - remainingTicks

        val progress: Float
            get() = if (durationTicks > 0L) (elapsedTicks.toFloat() / durationTicks.toFloat()).coerceIn(0f, 1f) else 1f

        val isCompleted: Boolean
            get() = remainingTicks <= 0L

        val remainingDuration: Duration
            get() = if (remainingTicks > 0L) (remainingTicks * 50L).milliseconds else Duration.ZERO

        fun resync(newRemainingTicks: Long) {
            targetServerTick = ServerTickEvents.currentServerTick + newRemainingTicks
        }

        internal fun tick() {
            if (isCompleted) {
                onTick?.invoke(this)
                onComplete?.invoke()
                unregister()
                return
            }

            if (elapsedTicks % interval == 0L) {
                onTick?.invoke(this)
            }
        }

        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
