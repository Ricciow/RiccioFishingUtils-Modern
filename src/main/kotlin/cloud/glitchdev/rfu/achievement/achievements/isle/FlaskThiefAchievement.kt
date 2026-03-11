package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.LootshareEvents.registerLootshareEvent
import gg.essential.universal.utils.toUnformattedString

@Achievement
object FlaskThiefAchievement : BaseAchievement() {
    override val id: String = "flask_thief"
    override val name: String = "Flask Thief"
    override val description: String = "Lootshare a Radioactive Vial!"
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners() {
        activeListeners.add(registerLootshareEvent { _, itens ->
            if(itens.any { it.itemStack.customName?.toUnformattedString()?.equals("Radioactive Vial") ?: false}) {
                complete()
            }
        })
    }
}