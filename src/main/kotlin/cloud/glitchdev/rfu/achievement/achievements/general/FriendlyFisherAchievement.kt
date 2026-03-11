package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot

@Achievement
object FriendlyFisherAchievement : NumericStageAchievement() {
    override val id: String = "friendly_fisher"
    override val name: String = "Friendly Fisher"
    override val description: String = "Equip a Bobbin' Time 3/4/5 full armor set."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.GENERAL
    override val targetStage: Int = 3

    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    private val bobbinRegex = Regex("""Bobbin'\s*Time\s+(III|IV|V|3|4|5)""", RegexOption.IGNORE_CASE)

    init {
        addStageInfo(1, "Polite Fisher", "Equip a Bobbin' Time 3+ full armor set.", AchievementDifficulty.EASY)
        addStageInfo(2, "Neighbourly Fisher", "Equip a Bobbin' Time 4+ full armor set.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Friendly Fisher", "Equip a Bobbin' Time 5 full armor set.", AchievementDifficulty.MEDIUM)
    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 100) {
            checkAll()
        })
    }

    private fun checkAll() {
        while (!isCompleted) {
            val count = getValidBobbinCount()
            currentCount = count
            if (count < targetCount) break
        }
    }

    private fun getValidBobbinCount(): Int {
        val player = mc.player ?: return 0

        return armorSlots.count { slot ->
            val armorPiece = player.getItemBySlot(slot)
            if (armorPiece.isEmpty) return@count false

            val lore = armorPiece[DataComponents.LORE] ?: return@count false

            lore.lines.any { lineComponent ->
                getBobbinLevelFromString(lineComponent.string) >= getTargetBobbinLevelForStage(currentStage)
            }
        }
    }

    private fun getBobbinLevelFromString(text: String): Int {
        val match = bobbinRegex.find(text) ?: return 0
        return when (val numeral = match.groupValues[1].uppercase()) {
            "III" -> 3
            "IV"  -> 4
            "V"   -> 5
            else  -> numeral.toIntOrNull() ?: 0
        }
    }

    override fun getTargetCountForStage(stage: Int): Int = 4

    private fun getTargetBobbinLevelForStage(stage: Int): Int = (stage + 2).coerceAtMost(5)
}