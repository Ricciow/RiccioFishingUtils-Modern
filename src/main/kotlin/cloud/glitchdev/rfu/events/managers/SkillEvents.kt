package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.constants.SkillType

object SkillEvents : AbstractEventManager<(skill: SkillType, xp: Long) -> Unit, SkillEvents.SkillXpUpdateEvent>() {
    override val runTasks: (SkillType, Long) -> Unit = { skill, xp ->
        safeExecution {
            tasks.forEach { event ->
                if (event.skill == null || event.skill == skill) {
                    event.callback(skill, xp)
                }
            }
        }
    }

    fun registerSkillXpUpdateEvent(
        skill: SkillType? = null,
        priority: Int = 20,
        callback: (skill: SkillType, xp: Long) -> Unit
    ): SkillXpUpdateEvent {
        return SkillXpUpdateEvent(skill, priority, callback).register()
    }

    class SkillXpUpdateEvent(
        val skill: SkillType?,
        priority: Int = 20,
        callback: (skill: SkillType, xp: Long) -> Unit
    ) : ManagedTask<(skill: SkillType, xp: Long) -> Unit, SkillXpUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
