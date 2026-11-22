package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

object PartyRequeueAlert {
    fun sendPartyDequeuedMessage(reason: String? = null) {
        if (!OtherSettings.partyRequeueAlert) return
        val text = if (reason != null) "Party dequeued ($reason)" else "Party dequeued"
        val message = TextUtils.rfupfLiteral("$text ", TextColor.LIGHT_RED)
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
