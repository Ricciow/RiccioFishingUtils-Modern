package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.AchievementProvider

abstract class NumericStageAchievement : StageAchievement() {
    abstract fun getTargetCountForStage(stage: Int): Long

    open val resetCountOnStageAdvance: Boolean = true

    override fun onStageChanged(oldStage: Int, newStage: Int) {
        if (resetCountOnStageAdvance) {
            currentCount = 0L
        }
    }

    open val targetCount: Long get() = getTargetCountForStage(currentStage)
    var currentCount: Long = 0L
        protected set(value) {
            field = value
            if (field >= targetCount) {
                advanceStage()
            } else {
                AchievementProvider.fireAchievementUpdated(this)
            }
        }

    override val targetProgress: Long get() = targetCount
    override val currentProgress: Long get() = currentCount

    override var _progress: Float = 0.0f
        get() = if (targetCount > 0L) currentCount.toFloat() / targetCount.toFloat() else 0.0f

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
        currentStage = targetStage + 1
    }

    override fun debugReset() {
        currentCount = 0L
        currentStage = 1
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        currentCount = (progressData["currentCount"] as? Number)?.toLong() ?: 0L
    }

    override fun saveState(): Map<String, Any> {
        val baseState = super.saveState().toMutableMap()
        baseState["currentCount"] = currentCount
        return baseState
    }
}
