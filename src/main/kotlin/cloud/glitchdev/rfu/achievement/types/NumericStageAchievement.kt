package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.AchievementProvider

abstract class NumericStageAchievement : StageAchievement() {
    abstract fun getTargetCountForStage(stage: Int): Int

    override var currentStage: Int = 1
        set(value) {
            field = value
            if (field > targetStage) {
                complete()
            }
        }

    open val targetCount: Int get() = getTargetCountForStage(currentStage)
    var currentCount: Int = 0
        protected set(value) {
            field = value
            if (field >= targetCount) {
                advanceStage()
            } else {
                AchievementProvider.fireAchievementUpdated(this)
            }
        }

    override val targetProgress: Int get() = targetCount
    override val currentProgress: Int get() = currentCount

    override var _progress: Float = 0.0f
        get() = if (targetCount > 0) currentCount.toFloat() / targetCount.toFloat() else 0.0f

    fun addProgress(amount: Int = 1) {
        if (isCompleted) return
        currentCount += amount
    }

    fun debugAddProgress(amount: Int = 1) {
        if (isCompleted) return
        markAsCheated()
        addProgress(amount)
    }

    override fun debugComplete() {
        markAsCheated()
        currentStage = targetStage + 1
    }

    override fun debugReset() {
        currentCount = 0
        currentStage = 1
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        currentCount = (progressData["currentCount"] as? Number)?.toInt() ?: 0
    }

    override fun saveState(): Map<String, Any> {
        val baseState = super.saveState().toMutableMap()
        baseState["currentCount"] = currentCount
        return baseState
    }
}
