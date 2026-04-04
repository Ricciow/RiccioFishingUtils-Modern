package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.events.managers.PartyEvents.registerPartyListChangedEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.model.party.FishingParty
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import java.util.concurrent.ConcurrentHashMap

@RFUFeature
object PartyFinderAlert : Feature {
    private val lastParties = ConcurrentHashMap.newKeySet<String>()
    private val pendingAlerts = ConcurrentHashMap<String, Long>()
    private var isFirstFetch = true

    override fun onInitialize() {
        registerPartyListChangedEvent { parties ->
            processParties(parties)
        }

        registerTickEvent(interval = 20) {
                checkPending()
        }
    }

    private fun processParties(currentParties: List<FishingParty>) {
        val currentPartiesUsers = currentParties.map { it.user }.toSet()

        if (isFirstFetch) {
            lastParties.addAll(currentPartiesUsers)
            isFirstFetch = false
            return
        }

        val newUsers = currentPartiesUsers.filter { it !in lastParties && it !in pendingAlerts.keys }
        val now = System.currentTimeMillis()

        newUsers.forEach { user ->
            pendingAlerts[user] = now + 15000
        }

        // Clean up pending and lastParties if they are no longer in currentParties
        pendingAlerts.keys.retainAll(currentPartiesUsers)
        lastParties.retainAll(currentPartiesUsers)
    }

    private fun checkPending() {
        val now = System.currentTimeMillis()
        val ready = pendingAlerts.filter { it.value <= now }.keys

        if (ready.isNotEmpty()) {
            val count = ready.size
            lastParties.addAll(ready)
            ready.forEach { pendingAlerts.remove(it) }

            if (OtherSettings.partyFinderAlert) {
                val message = Component.literal("\n")
                    .append(
                        TextUtils.rfupfLiteral("There are $WHITE$count$GOLD new parties!\n", GOLD)
                            .setStyle(
                                Style.EMPTY
                                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Open party finder /rfupf\n§8You can disable this message in the settings!\n§8Other -> Party Finder Alert")))
                                    .withClickEvent(ClickEvent.RunCommand("rfupf"))
                            )
                    )
                Chat.sendMessage(message)
            }
        }
    }
}
