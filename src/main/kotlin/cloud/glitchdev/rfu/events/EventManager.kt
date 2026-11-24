package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY

/**
 * Generates a scoped registration function for the task type declared by this manager.
 * Set [register] to false when the manager receives events without a startup hook.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EventManager(
    val register: Boolean = true,
    val name: String = "",
    val defaultPriority: Int = DEFAULT_EVENT_PRIORITY,
)
