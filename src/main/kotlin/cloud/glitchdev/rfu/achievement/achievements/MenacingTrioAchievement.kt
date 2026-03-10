package cloud.glitchdev.rfu.achievement.achievements

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent

@Achievement
object MenacingTrioAchievement : BaseAchievement() {
    override val id: String = "menacing_trio"
    override val name: String = "Menacing Trio"
    override val description: String = "Have a Thunder, a Jawbus and a Plhlegblast nearby at the same time."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners() {
        activeListeners.add(registerMobDetectEvent { entities ->
            var foundThunder = false
            var foundJawbus = false
            var foundPlhleg = false
            entities.forEach { entity ->
                if(entity.sbName == "Thunder") foundThunder = true
                if(entity.sbName == "Lord Jawbus") foundJawbus = true
                if(entity.sbName == "Plhlegblast") foundPlhleg = true
            }

            if(foundThunder && foundJawbus && foundPlhleg) {
                complete()
            }
        })
    }

}