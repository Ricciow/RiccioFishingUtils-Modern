package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.events.managers.SlotRenderEvents.registerSlotRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.Slot

@RFUFeature
object HighlightItensToSell : Feature {
    private val itemIds = setOf(
        "PITCHIN_KOI",
        "STAFF_OF_THE_VOLCANO",
        "BLADE_OF_THE_VOLCANO",
        "SLUG_BOOTS",
        "MOOGMA_LEGGINGS",
        "FLAMING_CHESTPLATE",
        "TAURUS_HELMET"
    )

    override fun onInitialize() {
        registerSlotRenderEvent(callback = ::render)
    }

    private fun render(graphics: GuiGraphicsExtractor, screen: AbstractContainerScreen<*>, slot: Slot) {
        if (!LavaFishing.highlightItemsToSell || !World.isInSkyblock) return
        if (screen.title.toUnformattedString() != "Trades") return
        val player = mc.player ?: return
        if (slot.container !== player.inventory || slot.item.isEmpty) return

        val tag = slot.item[DataComponents.CUSTOM_DATA]?.copyTag() ?: return
        val id = tag.getCompound("ExtraAttributes").orElse(null)?.getString("id")?.orElse(null)
            ?: tag.getString("id").orElse(null)
        if (id !in itemIds) return

        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x8000FF00.toInt())
    }
}
