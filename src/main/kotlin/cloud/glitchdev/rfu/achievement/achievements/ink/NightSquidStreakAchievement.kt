package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.ink.CollectionHour
import cloud.glitchdev.rfu.utils.Chat
import net.minecraft.network.chat.Component


@Achievement
object NightSquidStreakAchievement: NumericAchievement() {
    override val id: String = "night_squid_streak"
    override val name: String = "Pitch Black"
    override val description: String = "Catch 7 Night Squids in a row!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK
    override val targetCount: Long = 7L


    override fun setupListeners() {

        activeListeners.add(registerSeaCreatureCatchEvent
        { sc, _, _, _, _ ->
            if (sc == SeaCreatures.NIGHT_SQUID) {
                addProgress()
            } else {
                currentCount = 0L
            }
        })
    }

}