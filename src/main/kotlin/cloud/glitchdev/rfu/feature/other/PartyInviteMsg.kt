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
    val INVITE_REGEX = """^((lf|any(one)?|who('s)?)\s(((cish|fish|ink)(ing|ers?)?)|((crimson\s|ci\s)?p(arty|arties)?(\sinv(ite)?)?)|inv(ite)?)|p(arty)?|inv(ite)?|((p(arty)?|inv(ite)?)\sme)|(parties)|me+(\sme+)*)[?.!]?$""".toRegex(RegexOption.IGNORE_CASE)
    private const val PLAYER_REGEX = """(?:\[\d+\](?: .) )?(?:\[[A-Z]+\+*\] )?(\w{3,16})(?: \[\w+\])?"""
    val CHAT_REGEX = """(?:(Guild|Officer|Co-op) > )?$PLAYER_REGEX: (.+)""".toRegex()

    private val ME_ONLY_REGEX = """^(me+(\sme+)*)[?.!]?$""".toRegex(RegexOption.IGNORE_CASE)

    override fun onInitialize() {
        registerGameEvent(CHAT_REGEX) { _, _, matches ->
            if (!OtherSettings.partyInviteMsgs) return@registerGameEvent

            val groups = matches?.groupValues ?: return@registerGameEvent
            if (groups.size < 2) return@registerGameEvent

            val messageText = groups.last()
            val playerName = groups[groups.size - 2]
            val chatType = if (groups.size >= 3) groups[groups.size - 3] else ""

            if (!INVITE_REGEX.matches(messageText)) return@registerGameEvent
            if (playerName.isUser()) return@registerGameEvent

            val isMeMessage = ME_ONLY_REGEX.matches(messageText)
            val isPrivateChat = chatType.isNotEmpty()

            if (isMeMessage && !isPrivateChat) return@registerGameEvent

            val message = TextUtils.rfuLiteral("[Party $playerName]", TextColor.LIGHT_GREEN, TextEffects.BOLD)
                .withStyle(
                    Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("/p $playerName")))
                        .withClickEvent(ClickEvent.RunCommand("/p $playerName"))
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