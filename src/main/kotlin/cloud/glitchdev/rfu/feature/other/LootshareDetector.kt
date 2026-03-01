package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.item.ItemStack
import kotlin.math.abs

@RFUFeature
object LootshareDetector : Feature {
    private const val CORRELATION_WINDOW_MS = 100L
    private const val CLEANUP_THRESHOLD_MS = 500L
    private const val PLAYER_INVENTORY_ID = 0
    private val LOOTSHARE_REGEX = """LOOT SHARE You received loot for assisting (.+?)!""".toRegex()

    private val recentMessages = mutableListOf<PendingMessage>()
    private val recentItems = mutableListOf<PendingSlot>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var processingJob: Job? = null

    override fun onInitialize() {
        registerChatListener()
        registerSlotListener()
    }

    private fun registerChatListener() {
        registerGameEvent(LOOTSHARE_REGEX) { _, _, matches ->
            val playerName = matches?.groupValues?.getOrNull(1) ?: "<?>"
            val nowMs = System.currentTimeMillis()

            recentMessages.add(PendingMessage(nowMs, playerName))
            cleanOldEntries(nowMs)

            if (processingJob?.isActive != true) {
                processingJob = scope.launch {
                    delay(CORRELATION_WINDOW_MS + 20L)
                    Minecraft.getInstance().execute {
                        processAndNotifyBatch()
                    }
                }
            }
        }
    }

    private fun registerSlotListener() {
        registerSetSlotEvent { containerId, slot, item ->
            if (containerId != PLAYER_INVENTORY_ID || item.isEmpty) return@registerSetSlotEvent

            val player = Minecraft.getInstance().player ?: return@registerSetSlotEvent

            val previousItem = player.inventoryMenu.getSlot(slot).item

            val itemNameUnformatted = item.hoverName.toUnformattedString()
            val itemCount = item.count

            val prevItemNameUnformatted = previousItem.hoverName.toUnformattedString()
            val prevItemCount = previousItem.count

            val isNewItem = previousItem.isEmpty || prevItemNameUnformatted != itemNameUnformatted
            val isAmountIncreased = !isNewItem && itemCount > prevItemCount

            if (!isNewItem && !isAmountIncreased) return@registerSetSlotEvent

            val diffCount = if (isNewItem) itemCount else itemCount - prevItemCount

            val nowMs = System.currentTimeMillis()

            recentItems.add(PendingSlot(nowMs, containerId, slot, item.copy(), diffCount))

            cleanOldEntries(nowMs)
        }
    }

    private fun processAndNotifyBatch() {
        if (recentMessages.isEmpty() || !GeneralFishing.lootshareMessage) return

        val matchedItems = recentItems.filter { item ->
            recentMessages.any { msg -> abs(msg.timeMs - item.timeMs) <= CORRELATION_WINDOW_MS }
        }

        val contributors = recentMessages.map { it.playerName }.distinct().joinToString("&r, ")

        recentItems.removeAll(matchedItems)
        recentMessages.clear()

        if (matchedItems.isEmpty()) return

        val hoverComponent = Component.literal("&6&lContributing: &r$contributors".toMcCodes())

        val prefix = Component.literal("&6&lLOOT SHARE! &r".toMcCodes()).withStyle { style ->
            style.withHoverEvent(HoverEvent.ShowText(hoverComponent))
        }

        val finalMessage = Component.empty().append(prefix)

        matchedItems.forEachIndexed { index, slotData ->
            val itemStack = slotData.itemStack

            val itemComponent = itemStack.hoverName.copy().withStyle { style ->
                style.withHoverEvent(HoverEvent.ShowItem(itemStack))
            }

            val amountSuffix = if (slotData.diffCount > 1) {
                Component.literal(" &8${slotData.diffCount}x".toMcCodes())
            } else {
                Component.empty()
            }

            finalMessage.append(itemComponent).append(amountSuffix)

            if (index < matchedItems.size - 1) {
                finalMessage.append(Component.literal("&e, ".toMcCodes()))
            }
        }

        Chat.sendMessage(finalMessage)
    }

    private fun cleanOldEntries(nowMs: Long) {
        recentMessages.removeIf { nowMs - it.timeMs > CLEANUP_THRESHOLD_MS }
        recentItems.removeIf { nowMs - it.timeMs > CLEANUP_THRESHOLD_MS }
    }

    private data class PendingMessage(val timeMs: Long, val playerName: String)
    private data class PendingSlot(val timeMs: Long, val containerId: Int, val slot: Int, val itemStack: ItemStack, val diffCount: Int)
}