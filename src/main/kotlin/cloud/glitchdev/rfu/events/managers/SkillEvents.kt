package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.registration.DefaultNull
import cloud.glitchdev.rfu.constants.skyblock.SkillType

@EventManager(register = false)
object SkillEvents : AbstractEventManager<(skill: SkillType, xp: Long) -> Unit, SkillEvents.SkillXpUpdateEvent>() {
    override val runTasks: (SkillType, Long) -> Unit = { skill, xp ->
        tasks.forEach { event ->
            if (event.skill == null || event.skill == skill) {
                safeExecution {
                    event.callback(skill, xp)
                }
            }
        }
    }

    fun registerSkillXpUpdateEvent(
        skill: SkillType? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (skill: SkillType, xp: Long) -> Unit
    ): SkillXpUpdateEvent {
        return SkillXpUpdateEvent(skill, priority, callback).register()
    }

    class SkillXpUpdateEvent(
        @DefaultNull
        val skill: SkillType? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (skill: SkillType, xp: Long) -> Unit
    ) : ManagedTask<(skill: SkillType, xp: Long) -> Unit, SkillXpUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
