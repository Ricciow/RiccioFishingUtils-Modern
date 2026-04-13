package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.utils.World


@Achievement
object EasterAchievement: BaseAchievement() {
    override val id: String = "is_it_easter"
    override val name: String = "Is It Easter?"
    override val description: String = "Catch a Carrot King during Skyblock Month April!"
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.INK


    override fun setupListeners() {

        activeListeners.add(registerSeaCreatureCatchEvent
        { sc, _, _, _, _ ->
            if (sc == SeaCreatures.get("Carrot King")!!) {
                if(World.SBMonth == 4) { // april
                    complete()
                }
            }
        })
    }

}