package cloud.glitchdev.rfu.data.collections

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.HoverEvent

@AutoRegister
object CollectionGainTracker : RegisteredEvent {
    private val SACKS_MESSAGE_REGEX = """Sacks""".toRegex()
    private const val PLAYER_INVENTORY_ID = 0
    private const val FISHING_WINDOW_MS = 2000L
    
    private var lastFishingTime = 0L

    override fun register() {
        registerGameEvent(FishingSession.FISHING_XP_REGEX, isOverlay = true) { _, _, _ ->
            lastFishingTime = System.currentTimeMillis()
        }

        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            lastFishingTime = System.currentTimeMillis()
        }

        registerSetSlotEvent { containerId, slot, item ->
            if (containerId != PLAYER_INVENTORY_ID || item.isEmpty) return@registerSetSlotEvent
            // Hotbar/Inventory slots
            if (slot !in 9..44) return@registerSetSlotEvent

            if (System.currentTimeMillis() - lastFishingTime > FISHING_WINDOW_MS) return@registerSetSlotEvent

            val player = Minecraft.getInstance().player ?: return@registerSetSlotEvent
            val previousItem = player.inventoryMenu.getSlot(slot).item

            val itemName = item.hoverName.toUnformattedString()
            val prevItemName = previousItem.hoverName.toUnformattedString()
            
            val isNewItem = previousItem.isEmpty || prevItemName != itemName
            val isAmountIncreased = !isNewItem && item.count > previousItem.count

            if (!isNewItem && !isAmountIncreased) return@registerSetSlotEvent

            val diffCount = if (isNewItem) item.count else item.count - previousItem.count

            for (collItem in CollectionItem.entries) {
                if (collItem.displayName == itemName) {
                    CollectionsHandler.add(collItem, diffCount.toLong(), isSync = false)
                    break
                }
            }
        }

        // Track sack additions
        registerGameEvent(SACKS_MESSAGE_REGEX, isOverlay = false) { text, _, _ ->
            val hoverText = text.siblings.firstNotNullOfOrNull { component ->
                val hover = component.style.hoverEvent
                if (hover is HoverEvent.ShowText) {
                    return@firstNotNullOfOrNull hover.value.string
                }
            } as? String ?: return@registerGameEvent

            if ("Added" in hoverText) {
                for (item in CollectionItem.entries) {
                    val match = item.sackRegex.find(hoverText) ?: continue
                    val amount = match.groupValues[1].toLongOrNull() ?: continue
                    CollectionsHandler.add(item, amount, isSync = false)
                }
            }
        }
    }
}
