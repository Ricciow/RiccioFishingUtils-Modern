package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents


@AutoRegister
object TotalInkColl : RegisteredEvent {

    private val INK_SAC_REGEX = Regex("Ink Sac: ([\\d,]+)")
    private var pollingTask: TickEvents.TickEvent? = null
    private var pollStartTick = 0L
    private var hasSet = false // only set once per instance

    override fun register() {
        registerContainerOpenEvent { _, _ ->
            if (mc.screen?.title?.string == "Collections") {
                startPolling()
            }
        }
    }

    private fun startPolling() {
        pollingTask?.unregister()
        pollStartTick = mc.level?.gameTime ?: 0L
        pollingTask = registerTickEvent(interval = 20) { client ->
            val gameTime = client.level?.gameTime ?: return@registerTickEvent

            if (gameTime - pollStartTick > 200L || client.screen?.title?.string != "Collections") {
                stopPolling()
                return@registerTickEvent
            }

            val slots = client.player?.containerMenu?.slots ?: return@registerTickEvent
            for (slot in slots) {
                val lore = slot.item[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: continue
                for (line in lore) {
                    val match = INK_SAC_REGEX.find(line) ?: continue
                    val value = match.groupValues[1].replace(",", "").toIntOrNull() ?: continue
                    if(!hasSet) {
                        CollectionsHandler.totalInkSac = value.toLong()
                        hasSet = true
                    }
                    stopPolling()
                    return@registerTickEvent
                }
            }
        }
    }

    private fun stopPolling() {
        pollingTask?.unregister()
        pollingTask = null
    }

}