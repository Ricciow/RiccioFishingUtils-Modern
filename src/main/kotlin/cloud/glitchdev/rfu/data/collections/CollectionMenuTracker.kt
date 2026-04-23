package cloud.glitchdev.rfu.data.collections

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@AutoRegister
object CollectionMenuTracker : RegisteredEvent {
    private var pollingTask: TickEvents.TickEvent? = null
    private var pollStartTick = 0L

    override fun register() {
        registerContainerOpenEvent { _, _ ->
            val title = mc.screen?.title?.string ?: return@registerContainerOpenEvent
            if (title == "Collections") {
                startPolling()
            }
        }
    }

    private fun startPolling() {
        pollingTask?.unregister()
        pollStartTick = mc.level?.gameTime ?: 0L
        pollingTask = registerTickEvent(interval = 20) { client ->
            val gameTime = client.level?.gameTime ?: return@registerTickEvent

            if (gameTime - pollStartTick > 400L || client.screen == null) {
                stopPolling()
                return@registerTickEvent
            }

            val slots = client.player?.containerMenu?.slots ?: return@registerTickEvent
            for (slot in slots) {
                val lore = slot.item[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: continue
                for (line in lore) {
                    for (item in CollectionItem.entries) {
                        val match = item.collectionRegex.find(line) ?: continue
                        val value = match.groupValues[1].replace(",", "").toLongOrNull() ?: continue
                        
                        CollectionsHandler.set(item, value, isSync = true)
                    }
                }
            }
        }
    }

    private fun stopPolling() {
        pollingTask?.unregister()
        pollingTask = null
    }

}
