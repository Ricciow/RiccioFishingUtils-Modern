package cloud.glitchdev.rfu.feature.streak.challenge

import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.AbstractEventManager

abstract class BaseChallenge {
    abstract val id: String
    open val title: String = ""
    open val description: String = ""
    open val level: ChallengeLevel = ChallengeLevel.BASIC
    open val isMandatoryBase: Boolean = false

    protected val activeListeners = mutableListOf<AbstractEventManager.ManagedTask<*, *>>()

    abstract fun getTargetProgress(streakDays: Int): Int

    open fun getTitle(streakDays: Int): String = title

    open fun getDescription(streakDays: Int): String = description

    open fun getLevel(streakDays: Int): ChallengeLevel = level

    fun register() {
        ChallengeRegistry.register(this)
    }

    open fun setupListeners() {}

    fun unregisterListeners() {
        activeListeners.forEach { it.unregister() }
        activeListeners.clear()
    }

    protected fun addProgress(amount: Int = 1) {
        DailyStreakManager.addProgressForChallenge(id, amount)
    }
}
