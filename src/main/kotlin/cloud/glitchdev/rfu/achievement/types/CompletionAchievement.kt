package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.AchievementProvider
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.events.managers.AchievementUnlockedEvents.registerAchievementUnlockedEvent

abstract class CompletionAchievement : BaseAchievement() {
    abstract val achievements: List<BaseAchievement>

    private val BaseAchievement.isReallyCompleted: Boolean
        get() = this.isCompleted || AchievementHandler.getAchievementData(this.id)?.isCompleted == true

    override val currentProgress: Long
        get() = achievements.count { it.isReallyCompleted }.toLong()

    override val targetProgress: Long
        get() = achievements.size.toLong()

    override fun setupListeners() {
        updateProgress()

        activeListeners.add(registerAchievementUnlockedEvent { achievement ->
            if (achievements.any { it.id == achievement.id }) {
                updateProgress()
            }
        })
    }

    private fun updateProgress() {
        val total = targetProgress
        if (total == 0L) {
            _progress = 1.0f
            complete()
            return
        }

        val completedCount = currentProgress
        _progress = completedCount.toFloat() / total.toFloat()

        if (completedCount >= total) {
            complete()
        } else {
            AchievementProvider.fireAchievementUpdated(this)
        }
    }

    override fun debugComplete() {
        markAsCheated()
        for (achievement in achievements) {
            if (!achievement.isCompleted) {
                achievement.debugComplete()
            }
        }
        complete()
    }

    override fun debugReset() {
        super.debugReset()
        for (achievement in achievements) {
            achievement.debugReset()
        }
        updateProgress()
    }
}
