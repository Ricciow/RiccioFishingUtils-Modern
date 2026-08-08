package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent

@Achievement
object ShameOnYouAchievement : BaseAchievement() {
    override val id: String = "shame_on_you"
    override val name: String = "Shame on You!"
    override val description: String = "Go inside a hotspot wearing full Blaze or Frozen Blaze armor."
    override val type: AchievementType = AchievementType.OBFUSCATED
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override fun setupListeners() {
        activeListeners.add(registerArmorChangeEvent {
            checkCondition()
        })
        activeListeners.add(registerTickEvent(interval = 40) {
            checkCondition()
        })
    }

    private fun checkCondition() {
        val player = mc.player ?: return
        val userPos = player.position()
        HotSpotEvents.getHotspotAt(userPos) ?: return

        if (ArmorEvents.currentArmorSet.isWearingBlazeArmorSet) {
            complete()
        }
    }
}
