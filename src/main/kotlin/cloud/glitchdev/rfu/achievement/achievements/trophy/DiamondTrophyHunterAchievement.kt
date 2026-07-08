package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.EquipmentSlot

@Achievement
object DiamondTrophyHunterAchievement : StageAchievement() {
    override val id: String = "diamond_trophy_hunter"
    override val name: String = "Diamond Trophy Hunter"
    override val description: String = "Equip a full set of Diamond Hunter armor."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING
    override val targetStage: Int = 4

    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)

    init {
        addStageInfo(1, "Bronze Trophy Hunter", "Equip a full set of Bronze Hunter armor (or higher).", AchievementDifficulty.EASY)
        addStageInfo(2, "Silver Trophy Hunter", "Equip a full set of Silver Hunter armor (or higher).", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gold Trophy Hunter", "Equip a full set of Gold Hunter armor (or higher).", AchievementDifficulty.HARD)
        addStageInfo(4, "Diamond Trophy Hunter", "Equip a full set of Diamond Hunter armor.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 40) {
            val player = mc.player ?: return@registerTickEvent

            var minTier = 4
            var hasFullSet = true

            for (slot in armorSlots) {
                val item = player.getItemBySlot(slot)
                val customName = item.customName?.toUnformattedString()
                if (customName == null) {
                    hasFullSet = false
                    break
                }
                
                val tier = getHunterPieceTier(customName, slot)
                if (tier == 0) {
                    hasFullSet = false
                    break
                }
                if (tier < minTier) {
                    minTier = tier
                }
            }

            if (hasFullSet && minTier > 0) {
                while (minTier >= currentStage && !isCompleted) {
                    advanceStage()
                }
            }
        })
    }

    private fun getHunterPieceTier(itemName: String, slot: EquipmentSlot): Int {
        val expectedPieceName = when (slot) {
            EquipmentSlot.HEAD -> "Hunter Helmet"
            EquipmentSlot.CHEST -> "Hunter Chestplate"
            EquipmentSlot.LEGS -> "Hunter Leggings"
            EquipmentSlot.FEET -> "Hunter Boots"
            else -> return 0
        }
        if (!itemName.contains(expectedPieceName)) return 0

        return when {
            itemName.contains("Diamond Hunter") -> 4
            itemName.contains("Gold Hunter") -> 3
            itemName.contains("Silver Hunter") -> 2
            itemName.contains("Bronze Hunter") -> 1
            else -> 0
        }
    }
}
