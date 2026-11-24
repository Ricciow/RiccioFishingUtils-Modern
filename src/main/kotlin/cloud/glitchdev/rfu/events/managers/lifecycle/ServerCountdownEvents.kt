package cloud.glitchdev.rfu.events.managers.lifecycle

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.ServerTickEvents.registerServerTickEvent
import cloud.glitchdev.rfu.events.registration.Defaults
import cloud.glitchdev.rfu.events.registration.ScopedRegistration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@EventManager(generateRegistration = false)
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

    @ScopedRegistration
    @Defaults(
        "interval = 1L",
        "onTick = null",
        "onComplete = null",
    )
    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long = 1L,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        onTick: ((event: ServerCountdownEvent) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ): ServerCountdownEvent {
        return ServerCountdownEvent(priority, durationTicks, interval, onTick, onComplete).register()
    }

    @ScopedRegistration
    fun registerServerCountdownEvent(
        durationTicks: Long,
        priority: Int = DEFAULT_EVENT_PRIORITY,
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

    @ScopedRegistration
    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long,
        priority: Int = DEFAULT_EVENT_PRIORITY,
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

    @ScopedRegistration
    fun registerServerCountdownEvent(
        durationTicks: Long,
        interval: Long,
        onTick: (event: ServerCountdownEvent) -> Unit,
        onComplete: () -> Unit
    ): ServerCountdownEvent {
        return registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = interval,
            priority = DEFAULT_EVENT_PRIORITY,
            onTick = onTick,
            onComplete = onComplete
        )
    }

    class ServerCountdownEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        val durationTicks: Long,
        var interval: Long = 1L,
        val onTick: ((event: ServerCountdownEvent) -> Unit)? = null,
        val onComplete: (() -> Unit)? = null
    ) : ManagedTask<() -> Unit, ServerCountdownEvent>(priority, onComplete ?: {}) {

        val startServerTick: Long = ServerTickEvents.currentServerTick
        var targetServerTick: Long = startServerTick + durationTicks
            private set

        var isPaused: Boolean = false
            private set
        private var pausedRemainingTicks: Long = durationTicks

        val remainingTicks: Long
            get() = if (isPaused) {
                pausedRemainingTicks.coerceAtLeast(0L)
            } else {
                (targetServerTick - ServerTickEvents.currentServerTick).coerceAtLeast(0L)
            }

        val elapsedTicks: Long
            get() = durationTicks - remainingTicks

        val progress: Float
            get() = if (durationTicks > 0L) (elapsedTicks.toFloat() / durationTicks.toFloat()).coerceIn(0f, 1f) else 1f

        val isCompleted: Boolean
            get() = remainingTicks <= 0L

        val remainingDuration: Duration
            get() = if (remainingTicks > 0L) (remainingTicks * 50L).milliseconds else Duration.ZERO

        fun pause() {
            if (!isPaused && !isCompleted) {
                pausedRemainingTicks = remainingTicks
                isPaused = true
            }
        }

        fun unpause() {
            if (isPaused && !isCompleted) {
                targetServerTick = ServerTickEvents.currentServerTick + pausedRemainingTicks
                isPaused = false
            }
        }

        fun resync(newRemainingTicks: Long) {
            if (isPaused) {
                pausedRemainingTicks = newRemainingTicks
            } else {
                targetServerTick = ServerTickEvents.currentServerTick + newRemainingTicks
            }
        }

        internal fun tick() {
            if (isPaused) return

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
