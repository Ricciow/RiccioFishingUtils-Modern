package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object BuffCollectorAchievement : NumericAchievement() {
    override val id: String = "buff_collector"
    override val name: String = "Buff Collector"
    override val description: String = "Catch a sea creature in hotspots featuring each of the 5 unique buffs."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT
    override val targetCount: Long = 5

    private val collectedBuffs = mutableSetOf<String>()
    private val requiredBuffs = setOf(
        "Treasure Chance",
        "Fishing Speed",
        "Sea Creature Chance",
        "Double Hook Chance",
        "Trophy Fish Chance"
    )

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, _, hotspot ->
            if (hotspot != null) {
                val buff = hotspot.buff
                
                if (buff.isNotEmpty()) {
                    val matchedBuff = requiredBuffs.find { buff.contains(it, ignoreCase = true) }
                    if (matchedBuff != null && collectedBuffs.add(matchedBuff)) {
                        currentCount = collectedBuffs.size.toLong()
                    }
                }
            }
        })
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        collectedBuffs.clear()
        val savedBuffs = progressData["buffs"] as? List<String>
        if (savedBuffs != null) {
            collectedBuffs.addAll(savedBuffs)
        }
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["buffs"] = collectedBuffs.toList()
        return state
    }
}
