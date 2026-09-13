package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.TextUtils.rfuLiteral
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlinx.coroutines.delay
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.hypixel.data.type.GameType
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.jvm.optionals.getOrNull

@RFUFeature
object WalkthroughFeature : Feature {
    private var wasInSkyblock = false

    override fun onInitialize() {
        registerLocationEvent { event ->
            val inSkyblock = event.serverType.getOrNull() == GameType.SKYBLOCK || World.isInSkyblock
            if (!wasInSkyblock && inSkyblock) {
                if (!OtherSettings.walkthroughAcknowledged) {
                    Coroutines.launch {
                        delay(3000)
                        if (!OtherSettings.walkthroughAcknowledged && World.isInSkyblock) {
                            sendWalkthrough()
                        }
                    }
                }
            }
            wasInSkyblock = inSkyblock
        }

        registerDisconnectEvent {
            wasInSkyblock = false
        }
    }

    fun sendWalkthrough() {
        val acknowledge = Component.literal("${DARK_GREEN}$BOLD[I GET IT]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfuacknowledge"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to stop seeing this message on join.")))
            )

        val message = rfuLiteral("Welcome to Riccio Fishing Utils!", AQUAMARINE, BOLD)
            .append(Component.literal("\n${GOLD}RFU is designed to improve your fishing experience with useful alerts and tracking."))
            .append(Component.literal("\n${YELLOW}Here are some of the more useful commands/features:"))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}Party Finder: ${WHITE}Find parties with ease on /rfupf."))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}Achievements: ${WHITE}See achievements on /rfuachievements."))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}Dailies: ${WHITE}See daily challenges using /rfudailies."))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}HUD: ${WHITE}Move and resize HUD elements with /rfumove."))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}Settings: ${WHITE}Configure everything with /rfu."))
            .append(Component.literal("\n${GRAY}- ${AQUAMARINE}Other: ${WHITE}See all commands on /rfuhelp\n\n"))
            .append(acknowledge)

        Chat.sendMessage(message)
    }

    @Command
    object AcknowledgeCommand : AbstractCommand("rfuacknowledge") {
        override val description: String = "Acknowledges the RFU walkthrough."

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder.executes { context ->
                OtherSettings.walkthroughAcknowledged = true
                RiccioFishingUtils.saveConfig()

                context.source.sendFeedback(rfuLiteral("RFU has all these commands! But you'll no longer be bothered about it!", LIGHT_GREEN))
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
