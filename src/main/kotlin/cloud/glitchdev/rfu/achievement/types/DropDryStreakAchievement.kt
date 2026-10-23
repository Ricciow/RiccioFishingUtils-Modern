package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

abstract class DropDryStreakAchievement(private val drop: RareDrops) : NumericStageAchievement() {
    protected open val milestones: List<Long> = listOf(50L)
    protected open val stageNumerals: List<String> = listOf("I")

    abstract override val name: String
    override val description: String
        get() = "Catch ${milestones.joinToString("/")} ${creatureNames()} without dropping ${drop.displayName}."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val targetStage: Int get() = milestones.size
    override val resetCountOnStageAdvance: Boolean = false

    override fun getStageName(stage: Int): String? = stageNumerals.getOrNull(stage - 1)?.let { "$name $it" }

    override fun getStageDescription(stage: Int): String? = milestones.getOrNull(stage - 1)?.let {
        "Catch $it ${creatureNames()} without dropping ${drop.displayName}."
    }

    override fun getStageDifficulty(stage: Int): AchievementDifficulty? = when (stage) {
        1, 2 -> AchievementDifficulty.EASY
        3, 4 -> AchievementDifficulty.MEDIUM
        5, 6 -> AchievementDifficulty.HARD
        else -> null
    }

    private fun creatureNames(): String = drop.relatedScs.singleOrNull()?.plural
        ?: drop.relatedScNames.joinToString(" / ")

    override fun getTargetCountForStage(stage: Int): Long = milestones.getOrNull(stage - 1) ?: milestones.last()

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _, _, _, _ ->
            if (sc in drop.relatedScs) updateProgress()
        })
        activeListeners.add(registerCocoonEvent { sc ->
            if (sc in drop.relatedScs) updateProgress()
        })
        activeListeners.add(registerRareDropEvent { dropped, _ ->
            if (dropped == drop) updateProgress()
            true
        })

        updateProgress()
    }

    private fun updateProgress() {
        if (isCompleted) return
        val count = DropManager.dropHistory.getOrAdd(drop).currentCount.toLong()
        // An existing streak can already satisfy several stages when first loaded.
        do {
            currentCount = count
        } while (!isCompleted && count >= targetCount)
    }
}
