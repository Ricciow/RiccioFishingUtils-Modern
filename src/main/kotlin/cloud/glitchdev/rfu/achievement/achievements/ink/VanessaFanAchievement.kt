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
import cloud.glitchdev.rfu.utils.Chat
import net.minecraft.network.chat.Component


@Achievement
object VanessaFanAchievement: NumericStageAchievement() {
    override val id: String = "vanessa_fan"
    override val name: String = "#1 Vanessa Fan"
    override val description: String = "Buy rain at Vanessa!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK

    override val targetStage: Int = 4
    override val resetCountOnStageAdvance: Boolean = false

    private var RAIN_REGEX = """You added a minute of rain""".toRegex()


    private val MILESTONES = listOf(
        5L * 60, 25L * 60, 50L * 60, 75L * 60
    )

    private val MILESTONE_NAMES = listOf(
        "Makin' it Rain", "Forecast: Only Rain", "Rain Fanatic", "#1 Vanessa Fan"
    )


    init {
        MILESTONES.forEachIndexed { index, milestone ->
            var stage = index + 1

            var stageDifficulty = when {
                milestone >= 75L*60 -> AchievementDifficulty.VERY_HARD
                milestone >= 50L*60  -> AchievementDifficulty.HARD
                milestone >= 25L*60 -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Buy $milestone Minutes (${milestone/60} Hours) of Rain", stageDifficulty)

        }

    }

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(filter=RAIN_REGEX, isOverlay=false) {text, _, matches ->
            currentCount += 1

        })
    }



    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }


}