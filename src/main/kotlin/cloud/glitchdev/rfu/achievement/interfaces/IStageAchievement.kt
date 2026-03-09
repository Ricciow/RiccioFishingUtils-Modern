package cloud.glitchdev.rfu.achievement.interfaces

interface IStageAchievement : IAchievement {
    val currentStage: Int
    val targetStage: Int

    fun getStageName(stage: Int): String? = null
    fun getStageDescription(stage: Int): String? = null
}
