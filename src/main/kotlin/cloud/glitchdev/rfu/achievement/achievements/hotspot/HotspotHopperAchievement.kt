package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import kotlin.time.Duration.Companion.minutes

@Achievement
object HotspotHopperAchievement : NumericAchievement() {
    override val id: String = "hotspot_hopper"
    override val name: String = "Hotspot Hopper"
    override val description: String = "Catch a sea creature in a Water Hotspot and a Lava Hotspot within a minute."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    override val targetCount: Long = 2L

    private var lastHotspotType: LiquidTypes? = null
    private var lastHotspotTime: Long = 0L

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            if (currentCount == 1L && System.currentTimeMillis() - lastHotspotTime > 1.minutes.inWholeMilliseconds) {
                currentCount = 0L
                lastHotspotType = null
            }
        })

        activeListeners.add(registerSeaCreatureCatchEvent { sc, _, hotspot, _ ->
            if (hotspot != null) {
                val now = System.currentTimeMillis()
                val currentType = sc.liquidType

                if (lastHotspotType != null && lastHotspotType != currentType) {
                    if (now - lastHotspotTime <= 1.minutes.inWholeMilliseconds) {
                        currentCount = 2L
                        return@registerSeaCreatureCatchEvent
                    }
                }

                lastHotspotType = currentType
                lastHotspotTime = now
                currentCount = 1L
            }
        })
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        lastHotspotType = (progressData["lastType"] as? String)?.let { 
            try { LiquidTypes.valueOf(it) } catch (e: Exception) { null }
        }
        lastHotspotTime = (progressData["lastTime"] as? Number)?.toLong() ?: 0L
        
        // Reset progress if it's been more than a minute
        if (!isCompleted && lastHotspotType != null && (System.currentTimeMillis() - lastHotspotTime > 1.minutes.inWholeMilliseconds)) {
            currentCount = 0L
            lastHotspotType = null
        }
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["lastType"] = lastHotspotType?.name ?: ""
        state["lastTime"] = lastHotspotTime
        return state
    }
}
