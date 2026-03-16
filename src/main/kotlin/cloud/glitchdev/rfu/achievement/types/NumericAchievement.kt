package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.achievement.AchievementProvider

abstract class NumericAchievement : BaseAchievement() {
    abstract val targetCount: Long
    override val targetProgress: Long get() = targetCount
    
    var currentCount: Long = 0L
        protected set(value) {
            field = value
            _progress = if (targetCount > 0L) value.toFloat() / targetCount.toFloat() else 1.0f
            if (field >= targetCount) {
                complete()
            } else {
                AchievementProvider.fireAchievementUpdated(this)
            }
        }
    override val currentProgress: Long get() = currentCount

    fun addProgress(amount: Long = 1L) {
        if (isCompleted) return
        currentCount += amount
    }

    fun debugAddProgress(amount: Long = 1L) {
        if (isCompleted) return
        markAsCheated()
        addProgress(amount)
    }

    override fun debugComplete() {
        markAsCheated()
        currentCount = targetCount
    }

    override fun debugReset() {
        currentCount = 0L
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        currentCount = (progressData["currentCount"] as? Number)?.toLong() ?: 0L
    }

    override fun saveState(): Map<String, Any> {
        val baseState = super.saveState().toMutableMap()
        baseState["currentCount"] = currentCount
        return baseState
    }
}
