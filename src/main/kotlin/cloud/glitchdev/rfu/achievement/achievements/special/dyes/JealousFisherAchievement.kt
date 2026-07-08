package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@Achievement
object JealousFisherAchievement : BaseAchievement() {
    override val id: String = "jealous_fisher"
    override val name: String = "Jealous Fisher"
    override val description: String = "Witness another player drop a Carmine, Aquamarine, Treasure, Iceberg or Midnight Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.SPECIAL

    private val DYE_REGEX = """WOW! (.+) found (?:an? )?(Carmine|Aquamarine|Treasure|Midnight|Iceberg) Dye!""".toExactRegex()

    override fun setupListeners() {
        activeListeners.add(ChatEvents.registerGameEvent(filter = DYE_REGEX) { _, _, matches ->
            if (matches != null) {
                val username = matches.groupValues[1].removeRankTag()
                if (!username.isUser()) {
                    complete()
                }
            }
        })
    }
}
