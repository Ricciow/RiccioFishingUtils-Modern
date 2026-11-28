package cloud.glitchdev.rfu.data.collections

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.connection.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.inventory.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import gg.essential.universal.UScreen
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents

@RFUFeature
object CollectionMenuTracker : Feature {
    private data class Poll(val screen: AbstractContainerScreen<*>, val startTick: Long)
    private var activePoll: Poll? = null
    private var pollingScope: EventScope? = null

    override fun onInitialize(scope: EventScope) {
        scope.registerContainerOpenEvent { containerName, containerId, _ ->
            if (containerName == "Collections") {
                startPolling(scope, containerId)
            }
        }
        scope.registerDisconnectEvent { stopPolling() }
        scope.onClose { pollingScope = null }
        activePoll?.let { attachPolling(scope, it) }
    }

    private fun startPolling(scope: EventScope, containerId: Int) {
        val screen = UScreen.currentScreen as? AbstractContainerScreen<*> ?: return
        val menu = screen.menu
        if (menu.containerId != containerId) return

        val startTick = mc.level?.gameTime ?: return
        stopPolling()
        val poll = Poll(screen, startTick)
        activePoll = poll
        attachPolling(scope, poll)
    }

    private fun attachPolling(scope: EventScope, poll: Poll) {
        val polling = scope.child()
        pollingScope = polling
        polling.registerTickEvent { client ->
            val gameTime = client.level?.gameTime

            if (gameTime == null || gameTime - poll.startTick > 400L || UScreen.currentScreen !== poll.screen) {
                stopPolling()
                return@registerTickEvent
            }
            if (gameTime % 20L != 0L) return@registerTickEvent

            for (slot in poll.screen.menu.slots) {
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
        activePoll = null
        val scope = pollingScope
        pollingScope = null
        scope?.close()
    }
}
