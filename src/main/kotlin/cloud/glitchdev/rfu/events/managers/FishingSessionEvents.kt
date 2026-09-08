package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager

object FishingSessionEvents {

    fun registerFishingSessionStartEvent(
        priority: Int = 20,
        callback: () -> Unit
    ): FishingSessionStartEventManager.FishingSessionStartEvent {
        return FishingSessionStartEventManager.register(priority, callback)
    }

    fun registerFishingSessionEndEvent(
        priority: Int = 20,
        callback: () -> Unit
    ): FishingSessionEndEventManager.FishingSessionEndEvent {
        return FishingSessionEndEventManager.register(priority, callback)
    }

    fun registerFishingSessionPauseEvent(
        priority: Int = 20,
        callback: () -> Unit
    ): FishingSessionPauseEventManager.FishingSessionPauseEvent {
        return FishingSessionPauseEventManager.register(priority, callback)
    }

    fun registerFishingSessionResumeEvent(
        priority: Int = 20,
        callback: () -> Unit
    ): FishingSessionResumeEventManager.FishingSessionResumeEvent {
        return FishingSessionResumeEventManager.register(priority, callback)
    }

    object FishingSessionStartEventManager : AbstractEventManager<() -> Unit, FishingSessionStartEventManager.FishingSessionStartEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = 20, callback: () -> Unit): FishingSessionStartEvent {
            return FishingSessionStartEvent(priority, callback).register()
        }

        class FishingSessionStartEvent(
            priority: Int = 20,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionStartEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object FishingSessionEndEventManager : AbstractEventManager<() -> Unit, FishingSessionEndEventManager.FishingSessionEndEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = 20, callback: () -> Unit): FishingSessionEndEvent {
            return FishingSessionEndEvent(priority, callback).register()
        }

        class FishingSessionEndEvent(
            priority: Int = 20,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionEndEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object FishingSessionPauseEventManager : AbstractEventManager<() -> Unit, FishingSessionPauseEventManager.FishingSessionPauseEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = 20, callback: () -> Unit): FishingSessionPauseEvent {
            return FishingSessionPauseEvent(priority, callback).register()
        }

        class FishingSessionPauseEvent(
            priority: Int = 20,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionPauseEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object FishingSessionResumeEventManager : AbstractEventManager<() -> Unit, FishingSessionResumeEventManager.FishingSessionResumeEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task -> task.callback() }
            }
        }

        fun register(priority: Int = 20, callback: () -> Unit): FishingSessionResumeEvent {
            return FishingSessionResumeEvent(priority, callback).register()
        }

        class FishingSessionResumeEvent(
            priority: Int = 20,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, FishingSessionResumeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
