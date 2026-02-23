package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.network.PartyHttp
import cloud.glitchdev.rfu.model.party.FishingParty
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@RFUFeature
object PartyFinderAlert : Feature {
    private var lastParties: Set<String> = mutableSetOf()
    private var isFirstFetch = true

    override fun onInitialize() {
        registerTickEvent(interval = 6000) {
            if (!BackendSettings.backendAccepted) return@registerTickEvent
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
            val message = Component.literal("\n")
                .append(
                    TextUtils.rfupfLiteral("There are $WHITE${newParties.size}$GOLD new parties!\n",GOLD)
                    .setStyle(
                        Style.EMPTY
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("Open party finder /rfupf\n§8You can disable this message in the settings!\n§8Other -> Party Finder Alert")))
                            .withClickEvent(ClickEvent.RunCommand("rfupf"))
                    )
                )
            Chat.sendMessage(message)
        }

        lastParties = currentPartiesUsers
    }
}
