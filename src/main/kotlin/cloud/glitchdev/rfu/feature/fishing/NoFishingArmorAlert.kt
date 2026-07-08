package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot

@RFUFeature
object NoFishingArmorAlert : Feature {
    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    
    private var cachedArmorIds: List<String> = emptyList()
    private var cachedHasFishingArmor: Boolean = false

    private val statsKeywords = arrayOf(
        "Fishing Speed",
        "Trophy Chance",
        "Treasure Chance",
        "Double Hook Chance",
        "Sea Creature Chance"
    )

    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if (!GeneralFishing.noFishingArmorAlert) return@registerItemUsedEvent
            if (!FishingSession.isFishing) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            val player = mc.player ?: return@registerItemUsedEvent

            val currentArmorIds = armorSlots.map { slot ->
                val armorPiece = player.getItemBySlot(slot)
                if (armorPiece.isEmpty) {
                    ""
                } else {
                    val descriptionId = armorPiece.item.descriptionId
                    val customName = armorPiece.customName?.toUnformattedString() ?: ""
                    "$descriptionId:$customName"
                }
            }

            val hasFishingArmor = if (currentArmorIds == cachedArmorIds) {
                cachedHasFishingArmor
            } else {
                val isFishingArmorEquipped = armorSlots.any { slot ->
                    val armorPiece = player.getItemBySlot(slot)
                    if (armorPiece.isEmpty) return@any false
                    val lore = armorPiece[DataComponents.LORE] ?: return@any false
                    lore.lines.any { line ->
                        val plainText = line.toUnformattedString()
                        statsKeywords.any { keyword ->
                            plainText.contains(keyword, ignoreCase = true)
                        }
                    }
                }
                cachedArmorIds = currentArmorIds
                cachedHasFishingArmor = isFishingArmorEquipped
                isFishingArmorEquipped
            }

            if (!hasFishingArmor) {
                Title.showTitle("§c§lNO FISHING ARMOR!")
            }
        }
    }
}
