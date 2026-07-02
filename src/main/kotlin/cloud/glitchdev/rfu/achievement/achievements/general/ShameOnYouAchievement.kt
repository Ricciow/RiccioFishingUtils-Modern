package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.EquipmentSlot

@Achievement
object ShameOnYouAchievement : BaseAchievement() {
    override val id: String = "shame_on_you"
    override val name: String = "Shame on You!"
    override val description: String = "Go inside a hotspot wearing full Blaze or Frozen Blaze armor."
    override val type: AchievementType = AchievementType.HIDDEN
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.GENERAL

    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 40) {
            val player = mc.player ?: return@registerTickEvent
            val userPos = player.position()
            val userHotspot = HotSpotEvents.getHotspotAt(userPos) ?: return@registerTickEvent

            val hasFullBlazeSet = armorSlots.all { slot ->
                val item = player.getItemBySlot(slot)
                val customName = item.customName?.toUnformattedString() ?: return@all false
                isBlazePiece(customName, slot)
            }

            if (hasFullBlazeSet) {
                complete()
            }
        })
    }

    private fun isBlazePiece(itemName: String, slot: EquipmentSlot): Boolean {
        val expectedPieceName = when (slot) {
            EquipmentSlot.HEAD -> "Blaze Helmet"
            EquipmentSlot.CHEST -> "Blaze Chestplate"
            EquipmentSlot.LEGS -> "Blaze Leggings"
            EquipmentSlot.FEET -> "Blaze Boots"
            else -> return false
        }
        return itemName.contains(expectedPieceName)
    }
}
