package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import gg.essential.universal.utils.toFormattedString
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import kotlin.math.abs

@AutoRegister
object LootshareEvents : AbstractEventManager<(contributors: List<String>, items: List<LootshareEvents.LootshareItem>) -> Unit, LootshareEvents.LootshareEvent>(), RegisteredEvent {

    private const val CORRELATION_WINDOW_MS = 100L
    private const val CLEANUP_THRESHOLD_MS = 500L
    private const val PLAYER_INVENTORY_ID = 0
    private val LOOTSHARE_REGEX = """LOOT SHARE You received loot for assisting (.+?)!""".toRegex()

    private val recentMessages = mutableListOf<PendingMessage>()
    private val recentItems = mutableListOf<PendingSlot>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var processingJob: Job? = null

    override fun register() {
        registerGameEvent(LOOTSHARE_REGEX) { message, _, matches ->
            val playerName = message.toFormattedString().substringAfter("§r§e§lLOOT SHARE §r§fYou received loot for assisting ")
            val nowMs = System.currentTimeMillis()

            recentMessages.add(PendingMessage(nowMs, playerName))
            cleanOldEntries(nowMs)

            if (processingJob?.isActive != true) {
                processingJob = scope.launch {
                    delay(CORRELATION_WINDOW_MS + 20L)
                    safeExecution { processBatch() }
                }
            }
        }

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

            recentItems.add(PendingSlot(nowMs, slot, item.copy(), diffCount))
            cleanOldEntries(nowMs)
        }
    }

    private fun runTasks(contributors: List<String>, lootItems : List<LootshareItem>) {
        safeExecution {
            tasks.forEach { task -> task.callback(contributors, lootItems) }
        }
    }

    private fun processBatch() {
        if (recentMessages.isEmpty()) return

        val matchedItems = recentItems.filter { item ->
            recentMessages.any { msg -> abs(msg.timeMs - item.timeMs) <= CORRELATION_WINDOW_MS }
        }

        val contributors = recentMessages.map { it.playerName }.distinct()

        recentItems.removeAll(matchedItems)
        recentMessages.clear()

        if (matchedItems.isEmpty()) return

        val lootItems = matchedItems.map { LootshareItem(it.itemStack, it.diffCount) }

        runTasks(contributors, lootItems)
    }

    private fun cleanOldEntries(nowMs: Long) {
        recentMessages.removeIf { nowMs - it.timeMs > CLEANUP_THRESHOLD_MS }
        recentItems.removeIf { nowMs - it.timeMs > CLEANUP_THRESHOLD_MS }
    }

    fun registerLootshareEvent(
        priority: Int = 20,
        callback: (contributors: List<String>, items: List<LootshareItem>) -> Unit
    ): LootshareEvent {
        return LootshareEvent(priority, callback).register()
    }

    data class LootshareItem(val itemStack: ItemStack, val diffCount: Int)
    private data class PendingMessage(val timeMs: Long, val playerName: String)
    private data class PendingSlot(val timeMs: Long, val slot: Int, val itemStack: ItemStack, val diffCount: Int)

    class LootshareEvent(
        priority: Int = 20,
        callback: (contributors: List<String>, items: List<LootshareItem>) -> Unit
    ) : ManagedTask<(contributors: List<String>, items: List<LootshareItem>) -> Unit, LootshareEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
