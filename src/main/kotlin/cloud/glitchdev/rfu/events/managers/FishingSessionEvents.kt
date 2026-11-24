package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.feature.fishing.FishingSession.FishingType

object FishingSessionEvents {

    fun registerFishingSessionUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (fishingType: FishingType) -> Unit
    ): FishingSessionUpdatedEventManager.FishingSessionUpdatedEvent {
        return FishingSessionUpdatedEventManager.register(priority, callback)
    }

    fun registerFishingSessionStartEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionStartEventManager.FishingSessionStartEvent {
        return FishingSessionStartEventManager.register(priority, callback)
    }

    fun registerFishingSessionEndEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionEndEventManager.FishingSessionEndEvent {
        return FishingSessionEndEventManager.register(priority, callback)
    }

    fun registerFishingSessionPauseEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionPauseEventManager.FishingSessionPauseEvent {
        return FishingSessionPauseEventManager.register(priority, callback)
    }

    fun registerFishingSessionResumeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionResumeEventManager.FishingSessionResumeEvent {
        return FishingSessionResumeEventManager.register(priority, callback)
    }

    @EventManager(register = false)
    object FishingSessionUpdatedEventManager : AbstractEventManager<(FishingType) -> Unit, FishingSessionUpdatedEventManager.FishingSessionUpdatedEvent>() {
        override val runTasks: (FishingType) -> Unit = { fishingType ->
            safeExecution {
                tasks.forEach { task -> task.callback(fishingType) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingType) -> Unit): FishingSessionUpdatedEvent {
            return FishingSessionUpdatedEvent(priority, callback).register()
        }

        class FishingSessionUpdatedEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (FishingType) -> Unit
        ) : ManagedTask<(FishingType) -> Unit, FishingSessionUpdatedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object FishingSessionStartEventManager : AbstractEventManager<() -> Unit, FishingSessionStartEventManager.FishingSessionStartEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): FishingSessionStartEvent {
            return FishingSessionStartEvent(priority, callback).register()
        }

        class FishingSessionStartEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionStartEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object FishingSessionEndEventManager : AbstractEventManager<() -> Unit, FishingSessionEndEventManager.FishingSessionEndEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): FishingSessionEndEvent {
            return FishingSessionEndEvent(priority, callback).register()
        }

        class FishingSessionEndEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionEndEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object FishingSessionPauseEventManager : AbstractEventManager<() -> Unit, FishingSessionPauseEventManager.FishingSessionPauseEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): FishingSessionPauseEvent {
            return FishingSessionPauseEvent(priority, callback).register()
        }

        class FishingSessionPauseEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionPauseEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object FishingSessionResumeEventManager : AbstractEventManager<() -> Unit, FishingSessionResumeEventManager.FishingSessionResumeEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): FishingSessionResumeEvent {
            return FishingSessionResumeEvent(priority, callback).register()
        }

        class FishingSessionResumeEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionResumeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
