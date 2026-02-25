package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.isUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@RFUFeature
object PartyInviteMsg : Feature {
    val INVITE_REGEX = """^((lf|any(one)?|who('s)?)\s(((cish|fish|ink)(ing)?)|((crimson\s|ci\s)?p(arty|arties)?(\sinv(ite)?)?)|inv(ite)?)|p(arty)?|inv(ite)?|((p(arty)?|inv(ite)?)\sme)|(parties)|me+(\sme+)*)[?.!]?$""".toRegex()
    private const val PLAYER_REGEX = """(?:\[\d+\](?: .) )?(?:\[[A-Z]+\+*\] )?([0-9a-zA-Z_]{3,16})(?: \[[0-9a-zA-Z_]+\])?"""
    val CHAT_REGEX = """(?:(?:Guild|Officer) > )?$PLAYER_REGEX: (.+)""".toRegex()

    override fun onInitialize() {
        registerGameEvent(CHAT_REGEX) { _, _, matches ->
            if(!OtherSettings.partyInviteMsgs) return@registerGameEvent
            val groups = matches?.groupValues ?: return@registerGameEvent
            if(!INVITE_REGEX.matches(groups.getOrNull(2) ?: "")) return@registerGameEvent
            val name = groups.getOrNull(1) ?: return@registerGameEvent
            if(name.isUser()) return@registerGameEvent

            val message = TextUtils.rfuLiteral("[Party $name]", TextColor.LIGHT_GREEN, TextEffects.BOLD)
                .withStyle(
                    Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("/p $name")))
                        .withClickEvent(ClickEvent.RunCommand("/p $name"))
                )

            CoroutineScope(Dispatchers.Default).launch {
                delay(10)
                mc.execute {
                    Chat.sendMessage(message)
                }
            }
        }
    }
}