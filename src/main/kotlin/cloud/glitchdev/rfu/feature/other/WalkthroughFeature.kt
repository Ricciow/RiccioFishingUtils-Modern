package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.TextUtils.rfuLiteral
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.delay
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@AutoRegister
object WalkthroughFeature : RegisteredEvent {
    private var isHidingMessages = false
    private val hiddenMessages = mutableListOf<Component>()
    private val walkthroughSentMessages = mutableSetOf<String>()

    override fun register() {
        registerJoinEvent(1500) { wasConnected ->
            if (!wasConnected && !OtherSettings.walkthroughAcknowledged) {
                sendWalkthrough()
            }
        }

        registerAllowGameEvent { text, overlay, _ ->
            if (isHidingMessages && !isWalkthroughMessage(text) && !overlay) {
                hiddenMessages.add(text)
                return@registerAllowGameEvent false
            }
            true
        }
    }

    private fun isWalkthroughMessage(text: Component): Boolean {
        return walkthroughSentMessages.contains(text.toUnformattedString())
    }

    private fun send(message: Component) {
        walkthroughSentMessages.add(message.toUnformattedString())
        Chat.sendMessage(message)
    }

    fun sendWalkthrough() {
        if (isHidingMessages) return
        
        hiddenMessages.clear()
        walkthroughSentMessages.clear()
        isHidingMessages = true

        Coroutines.launch {
            try {
                send(rfuLiteral("Welcome to Riccio Fishing Utils!", AQUAMARINE, BOLD))
                delay(2000)
                send(Component.literal("${GOLD}RFU is designed to improve your fishing experience with useful alerts and tracking."))
                send(Component.literal("${YELLOW}Here are some of the more useful commands/features:"))
                delay(4000)
                send(Component.literal("${GRAY}- ${AQUAMARINE}Party Finder: ${WHITE}Find parties with ease on /rfupf."))
                delay(3000)
                send(Component.literal("${GRAY}- ${AQUAMARINE}Achievements: ${WHITE}See achievements on /rfuachievements."))
                delay(3000)
                send(Component.literal("${GRAY}- ${AQUAMARINE}HUD: ${WHITE}Move and resize HUD elements with /rfumove."))
                delay(3000)
                send(Component.literal("${GRAY}- ${AQUAMARINE}Settings: ${WHITE}Configure everything with /rfu."))
                delay(3000)
                send(Component.literal("${GRAY}- ${AQUAMARINE}Other: ${WHITE}See all commands on /rfuhelp"))
                delay(3000)

                val acknowledge = Component.literal("${DARK_GREEN}$BOLD[I GET IT]")
                    .setStyle(
                        Style.EMPTY
                            .withClickEvent(ClickEvent.RunCommand("/rfuacknowledge"))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to stop seeing this message on join.")))
                    )

                send(acknowledge)
                delay(5000)
                send(Component.literal("${LIGHT_RED}All messages were hid during this walkthrough, re-sending them in 5s"))
                
                delay(5000)
            } finally {
                isHidingMessages = false
                val captured = hiddenMessages.toList()
                hiddenMessages.clear()
                captured.forEach { message ->
                    Chat.sendMessage(message)
                }
                delay(1000)
                walkthroughSentMessages.clear()
            }
        }
    }

    @Command
    object AcknowledgeCommand : AbstractCommand("rfuacknowledge") {
        override val description: String = "Acknowledges the RFU walkthrough."

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder.executes { context ->
                OtherSettings.walkthroughAcknowledged = true
                RiccioFishingUtils.saveConfig()
                
                context.source.sendFeedback(rfuLiteral("Walkthrough acknowledged! You won't see this message again.", LIGHT_GREEN))
                1
            }
        }
    }

    @Command
    object WalkthroughCommand : AbstractCommand("rfuwalkthrough") {
        override val description: String = "Shows the RFU walkthrough again."

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder.executes { _ ->
                sendWalkthrough()
                1
            }
        }
    }
}
