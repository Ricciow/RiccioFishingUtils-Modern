package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.achievement.AchievementProvider
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement

abstract class StageAchievement : BaseAchievement(), IStageAchievement {
    abstract override val targetStage: Int
    override val targetProgress: Int get() = targetStage
    
    protected val stageNames = mutableMapOf<Int, String>()
    protected val stageDescriptions = mutableMapOf<Int, String>()
    protected val stageDifficulties = mutableMapOf<Int, AchievementDifficulty>()

    override fun getStageName(stage: Int): String? = stageNames[stage]
    override fun getStageDescription(stage: Int): String? = stageDescriptions[stage]
    override fun getStageDifficulty(stage: Int): AchievementDifficulty? = stageDifficulties[stage]

    protected fun addStageInfo(stage: Int, name: String, description: String) {
        stageNames[stage] = name
        stageDescriptions[stage] = description
    }

    protected fun addStageInfo(stage: Int, name: String, description: String, difficulty: AchievementDifficulty) {
        stageNames[stage] = name
        stageDescriptions[stage] = description
        stageDifficulties[stage] = difficulty
    }

    override var currentStage: Int = 1
        protected set(value) {
            field = value
            _progress = if (targetStage > 1) (value - 1).toFloat() / (targetStage - 1).toFloat() else 1.0f
            
            if (field >= targetStage) {
                complete()
            }
        }
    override val currentProgress: Int get() = currentStage
        
    fun advanceStage() {
        if (isCompleted) return
        currentStage += 1
        AchievementProvider.fireAchievementStageUnlocked(this)
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        val savedStage = (progressData["currentStage"] as? Number)?.toInt() ?: 1
        currentStage = savedStage.coerceAtLeast(1)
    }

    override fun saveState(): Map<String, Any> {
        val baseState = super.saveState().toMutableMap()
        baseState["currentStage"] = currentStage
        return baseState
    }
}
