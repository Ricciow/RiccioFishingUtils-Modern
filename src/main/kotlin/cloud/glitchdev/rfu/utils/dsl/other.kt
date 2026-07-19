package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot

fun getResource(resource : String) : Identifier {
    val result = Identifier.fromNamespaceAndPath(MOD_ID, resource)
    return result
}

fun parseResource(id :  String) : Identifier? {
    val result = Identifier.tryParse(id)
    return result
}

fun isWearingTrophyHunterArmor(): Boolean {
    val player = mc.player ?: return false
    val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    return armorSlots.all { slot ->
        val item = player.getItemBySlot(slot)
        val customName = item.customName?.toUnformattedString() ?: return@all false
        val expectedPieceName = when (slot) {
            EquipmentSlot.HEAD -> "Hunter Helmet"
            EquipmentSlot.CHEST -> "Hunter Chestplate"
            EquipmentSlot.LEGS -> "Hunter Leggings"
            EquipmentSlot.FEET -> "Hunter Boots"
            else -> return@all false
        }
        customName.contains(expectedPieceName) && (
                customName.contains("Bronze Hunter") ||
                        customName.contains("Silver Hunter") ||
                        customName.contains("Gold Hunter") ||
                        customName.contains("Diamond Hunter")
                )
    }
}