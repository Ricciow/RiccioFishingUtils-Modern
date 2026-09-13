package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.PartyEvents.registerOnPartyChangeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@RFUFeature
object PartyRequeueAlert : Feature {
    private var wasFull = false

    override fun onInitialize() {
        registerDisconnectEvent {
            wasFull = false
        }

        registerOnPartyChangeEvent { inParty, isLeader, _, _ ->
            if (!inParty) {
                wasFull = false
                return@registerOnPartyChangeEvent
            }

            val previousParty = PartyWebSocket.getPreviousParty()
            val maxPlayers = previousParty?.players?.max ?: 6
            val memberCount = Party.memberCount

            if (memberCount >= maxPlayers) {
                wasFull = true
            } else if (wasFull && memberCount > 0) {
                wasFull = false
                if (isLeader &&
                    PartyWebSocket.myParty == null &&
                    OtherSettings.partyRequeueAlert &&
                    BackendSettings.backendAccepted &&
                    World.isInSkyblock &&
                    !World.isOnAlpha &&
                    previousParty != null
                ) {
                    sendPartyNoLongerFullMessage()
                }
            }
        }
    }

    fun sendPartyDequeuedMessage(reason: String? = null) {
        if (!OtherSettings.partyRequeueAlert) return
        val text = if (reason != null) "Party dequeued ($reason)" else "Party dequeued"
        val message = TextUtils.rfupfLiteral("$text ", TextColor.LIGHT_RED)
        message.append(createRequeueButton())
        Chat.sendMessage(message)
    }

    fun sendPartyNoLongerFullMessage() {
        if (!OtherSettings.partyRequeueAlert) return
        val message = TextUtils.rfupfLiteral("Party is no longer full! ", TextColor.YELLOW)
        message.append(createRequeueButton())
        Chat.sendMessage(message)
    }

    private fun createRequeueButton(): Component {
        return Component.literal("${TextColor.LIGHT_GREEN}${TextEffects.BOLD}[Requeue]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfurequeue"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("${TextColor.YELLOW}Click to requeue your party!")))
            )
    }
}
