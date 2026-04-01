package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.ink.CollectionHour
import cloud.glitchdev.rfu.utils.Chat
import net.minecraft.network.chat.Component


@Achievement
object InkObsessedAchievement: NumericStageAchievement() {
    override val id: String = "ink_obsessed"
    override val name: String = "Ink Obsessed"
    override val description: String = "Gain ink collection in one session!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK

    override val targetStage: Int = 4
    override val resetCountOnStageAdvance: Boolean = false


    private val MILESTONES = listOf(
        25_000L, 50_000L, 100_000L, 250_000L
    )

    private val MILESTONE_NAMES = listOf(
        "Locked In", "Need More Ink!", "Time for a Break..?", "Ink Obsessed"
    )


    init {
        MILESTONES.forEachIndexed { index, milestone ->
            var stage = index + 1
            var formatted = formatXp(milestone)

            var stageDifficulty = when {
                milestone >= 250_000L -> AchievementDifficulty.VERY_HARD
                milestone >= 100_000L  -> AchievementDifficulty.HARD
                milestone >= 50_000L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Gain $formatted ink collection in a single session", stageDifficulty)

        }

    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            val inkSession = CollectionHour.totalInk
            currentCount = inkSession.toLong()

        })
    }



    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }

    private fun formatXp(xp: Long): String {
        return when {
            xp >= 1_000_000_000L -> "${xp / 1_000_000_000.0}B"
            xp >= 1_000_000L -> "${xp / 1_000_000.0}M"
            xp >= 1_000L -> "${xp / 1_000.0}k"
            else -> xp.toString()
        }.replace(".0", "")
    }


}