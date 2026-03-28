package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.utils.Chat


import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.HoverEvent
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes

import kotlin.time.Duration


@RFUFeature
object CollectionHour : Feature {
    var INK_COLL_REGEX = """Sacks""".toRegex()

    private var lastInkEvent: Instant = Instant.DISTANT_PAST
    val startFishing: Instant get() = FishingSession.startFishing


    var totalInk: Double = 0.0
        private set

    var currentInkPerHour: Double = 0.0
        private set

    var pausedAt: Instant? = null
        private set

    var totalActiveTime: Duration = Duration.ZERO
        private set

    var effectiveElapsed: Duration = Duration.ZERO

    var first = true
        private set

    var sentWarning = false
        private set

    override fun onInitialize() {
        registerGameEvent(INK_COLL_REGEX, isOverlay = false) { text, _, matches ->
            first = true
            text.siblings.forEachIndexed { index, component ->
                when (val hover = component.style.hoverEvent) {
                    is HoverEvent.ShowText ->
                        if("Ink" in hover.value.string && "Added" in hover.value.string) {
                            val inkRegex = """\+(\d+) Ink Sac""".toRegex()

                            val match = inkRegex.find(hover.value.string)
                            val inkAmount = match?.groupValues?.get(1)?.toDoubleOrNull()
                            if(inkAmount != null && first) {
                                first = false
                                handleInk(inkAmount)

                            }


                        }
                }
            }


        }   

        registerTickEvent(interval = 20) {
            updateRate()
        }
    }

    @Command
    object ResetCommand : SimpleCommand("rfuresetinkph") {
        override val description: String = "Resets your current Ink/h tracker."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            resetSession()
            updateRate()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("The Ink/h tracker has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }

    private fun handleInk(ink: Double) {
        val now = Clock.System.now()

        if (pausedAt == null && lastInkEvent != Instant.DISTANT_PAST) {
            totalActiveTime += now - lastInkEvent
        }

        // first sack message read
        if(first) {
            totalActiveTime += now - startFishing
            first = false
        }

        if(CollectionsHandler.totalInkSac < 1000 && !sentWarning) {
            Chat.sendMessage(TextUtils.rfuLiteral("Set total ink collection by checking your ink ranking in collections menu!"))
            sentWarning = true
        }

        pausedAt = null
        lastInkEvent = now
        totalInk += ink
        CollectionsHandler.totalInkSac += ink.toInt()
        updateRate()

    }

    private fun updateRate() {
        val now = Clock.System.now()
        val limit = InkFishing.fishingTimeAFK.minutes


        if (lastInkEvent != Instant.DISTANT_PAST && (now - lastInkEvent) > limit) {
            pausedAt = lastInkEvent
        }

        if (startFishing == Instant.DISTANT_PAST && pausedAt == null) {
            currentInkPerHour = 0.0
            return
        }

        effectiveElapsed = totalActiveTime + if (pausedAt == null && lastInkEvent != Instant.DISTANT_PAST) {
            now-lastInkEvent
        } else {
            Duration.ZERO
        }

        currentInkPerHour = (totalInk / effectiveElapsed.inWholeSeconds) * 3600

    }

    private fun resetSession() {
        lastInkEvent = Instant.DISTANT_PAST
        totalActiveTime = Duration.ZERO
        pausedAt = null
        totalInk = 0.0
        currentInkPerHour = 0.0
    }

}