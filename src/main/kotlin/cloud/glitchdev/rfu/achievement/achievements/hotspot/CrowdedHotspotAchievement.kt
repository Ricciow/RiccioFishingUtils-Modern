package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.fishing.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.entity.registerPlayerDetectEvent

@Achievement
object CrowdedHotspotAchievement : NumericAchievement() {
    override val id: String = "crowded_hotspot"
    override val name: String = "Crowded Hotspot"
    override val description: String = "Stand in a Hotspot with 6 or more players."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT
    override val targetCount: Long = 6L

    override fun setupListeners(scope: EventScope) {
        scope.registerPlayerDetectEvent { players ->
            val user = mc.player ?: return@registerPlayerDetectEvent
            val userPos = user.position()
            val userHotspot = HotSpotEvents.getHotspotAt(userPos)

            if(userHotspot == null) {
                currentCount = 0
                return@registerPlayerDetectEvent
            }

            currentCount = players.count { player ->
                val playerHotspot = HotSpotEvents.getHotspotAt(player.position())
                playerHotspot?.uuid == userHotspot.uuid
            } + 1L
        }
    }

}
