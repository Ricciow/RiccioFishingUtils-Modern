package cloud.glitchdev.rfu.events.managers.ui

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.announcement.Announcement

@EventManager(register = false)
object AnnouncementEvents : AbstractEventManager<(announcement: Announcement?) -> Unit, AnnouncementEvents.AnnouncementUpdateEvent>() {
    override val runTasks: (Announcement?) -> Unit = { announcement ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(announcement)
            }
        }
    }

    fun registerAnnouncementUpdateEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Announcement?) -> Unit): AnnouncementUpdateEvent {
        return AnnouncementUpdateEvent(priority, callback).register()
    }

    class AnnouncementUpdateEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Announcement?) -> Unit
    ) : ManagedTask<(Announcement?) -> Unit, AnnouncementUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }

    fun trigger(announcement: Announcement?) {
        runTasks(announcement)
    }
}
