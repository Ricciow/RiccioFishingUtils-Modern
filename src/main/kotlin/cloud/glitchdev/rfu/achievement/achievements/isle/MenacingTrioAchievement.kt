package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent

@Achievement
object MenacingTrioAchievement : NumericAchievement() {
    override val id: String = "menacing_trio"
    override val name: String = "Menacing Trio"
    override val description: String = "Have a Thunder, a Jawbus and a Plhlegblast nearby at the same time."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetCount: Long = 3L

    override fun setupListeners() {
        activeListeners.add(registerMobDetectEvent { entities ->
            var found = 0L
            if(entities.any { it.sbName == "Thunder" }) found++
            if(entities.any { it.sbName == "Lord Jawbus" }) found++
            if(entities.any { it.sbName == "Plhlegblast" }) found++

            currentCount = found
        })
    }

}