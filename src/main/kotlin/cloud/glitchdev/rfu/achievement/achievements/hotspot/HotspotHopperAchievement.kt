package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import kotlin.time.Duration.Companion.minutes

@Achievement
object HotspotHopperAchievement : BaseAchievement() {
    override val id: String = "hotspot_hopper"
    override val name: String = "Hotspot Hopper"
    override val description: String = "Catch a sea creature in a Water Hotspot and a Lava Hotspot within a minute."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    private var lastHotspotType: LiquidTypes? = null
    private var lastHotspotTime: Long = 0L

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _, hotspot, _ ->
            if (hotspot != null) {
                val now = System.currentTimeMillis()
                val currentType = sc.liquidType

                if (lastHotspotType != null && lastHotspotType != currentType) {
                    if (now - lastHotspotTime <= 1.minutes.inWholeMilliseconds) {
                        complete()
                        return@registerSeaCreatureCatchEvent
                    }
                }

                lastHotspotType = currentType
                lastHotspotTime = now
            }
        })
    }

    override fun loadState(progressData: Map<String, Any>) {
        lastHotspotType = (progressData["lastType"] as? String)?.let { 
            try { LiquidTypes.valueOf(it) } catch (e: Exception) { null }
        }
        lastHotspotTime = (progressData["lastTime"] as? Number)?.toLong() ?: 0L
    }

    override fun saveState(): Map<String, Any> {
        return mapOf(
            "lastType" to (lastHotspotType?.name ?: ""),
            "lastTime" to lastHotspotTime
        )
    }
}
