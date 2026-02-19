package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.network.PartyHttp
import cloud.glitchdev.rfu.model.party.FishingParty

@RFUFeature
object PartyFinderAlert : Feature {
    private var lastParties: Set<String> = mutableSetOf()
    private var isFirstFetch = true

    override fun onInitialize() {
        registerTickEvent(interval = 6000) {
            if (!OtherSettings.partyFinderAlert) return@registerTickEvent

            PartyHttp.getParties { parties ->
                if (parties != null) {
                    processParties(parties)
                }
            }
        }
    }

    private fun processParties(currentParties: List<FishingParty>) {
        val currentPartiesUsers = currentParties.map { it.user }.toSet()

        if (isFirstFetch) {
            lastParties = currentPartiesUsers
            isFirstFetch = false
            return
        }

        val newParties = currentPartiesUsers.filter { it !in lastParties }

        if (newParties.isNotEmpty()) {
            val message = TextUtils.rfupfLiteral("There are $WHITE${newParties.size}$GOLD new parties!",GOLD)
            Chat.sendMessage(message)
        }

        lastParties = currentPartiesUsers
    }
}
