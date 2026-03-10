package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.BaseAchievement

abstract class NumericAchievement : BaseAchievement() {
    abstract val targetCount: Int
    override val targetProgress: Int get() = targetCount
    
    var currentCount: Int = 0
        protected set(value) {
            field = value
            _progress = if (targetCount > 0) value.toFloat() / targetCount.toFloat() else 1.0f
            if (field >= targetCount) {
                complete()
            }
        }
    override val currentProgress: Int get() = currentCount

    fun addProgress(amount: Int = 1) {
        if (isCompleted) return
        currentCount += amount
    }

    override fun debugComplete() {
        currentCount = targetCount
    }

    override fun debugReset() {
        currentCount = 0
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        val savedCount = (progressData["currentCount"] as? Number)?.toInt() ?: 0
        currentCount = savedCount
    }

    override fun saveState(): Map<String, Any> {
        val baseState = super.saveState().toMutableMap()
        baseState["currentCount"] = currentCount
        return baseState
    }
}
