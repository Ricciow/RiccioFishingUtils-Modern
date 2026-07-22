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

private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)

fun isWearingTrophyHunterArmor(): Boolean {
    return mc.player?.let { player ->
        armorSlots.any { slot -> player.getItemBySlot(slot).hasDescriptionText("Tiered Bonus: Peace Treaty (2/2)") }
    } == true
}