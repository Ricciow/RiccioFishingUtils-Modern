package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@AutoRegister
object BaitEventManager : AbstractEventManager<(Bait?, Int) -> Unit, BaitEventManager.BaitChangedEvent>(), RegisteredEvent {
    var lastBait: Bait? = null
    var lastCount: Int = 0
    private var currentCount: Int = 0

    private const val PLAYER_INVENTORY_ID = 0
    private val BAIT_COUNT_REGEX = """Bait Remaining: ([\d,]+)""".toExactRegex()

    override fun register() {
        registerSetSlotEvent { id, slot, item ->
            if (id != PLAYER_INVENTORY_ID || item.isEmpty) return@registerSetSlotEvent
            if (slot != 44) return@registerSetSlotEvent
            val bait = Bait.fromName(item.hoverName.string)

            if (bait == null) {
                lastBait = null
                lastCount = 0
                currentCount = 0
                runTasks(null, 0)
                return@registerSetSlotEvent
            }

            lastBait = bait

            val loreLines = item[DataComponents.LORE]?.lines ?: return@registerSetSlotEvent
            val count = loreLines.mapNotNull {
                BAIT_COUNT_REGEX.find(it.toUnformattedString())?.groupValues?.getOrNull(1)?.replace(",", "")
            }.getOrNull(0)?.toIntOrNull() ?: 0

            if (lastCount == 0 || lastCount <= count) {
                lastCount = count
            } else {
                lastCount = currentCount
            }
            currentCount = count

            runTasks(bait, count)
        }
    }

    override val runTasks: (Bait?, Int) -> Unit = { bait, count ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(bait, count)
            }
        }
    }

    fun registerBaitChangedEvent(priority: Int = 20, callback: (Bait?, Int) -> Unit): BaitChangedEvent {
        return BaitChangedEvent(priority, callback).register()
    }

    class BaitChangedEvent(
        priority: Int = 20,
        callback: (Bait?, Int) -> Unit
    ) : ManagedTask<(Bait?, Int) -> Unit, BaitChangedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
