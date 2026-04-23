package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.ink.InkSessionTracker
import cloud.glitchdev.rfu.feature.mob.SeaCreatureHour
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@RFUFeature
object FishingSession : Feature {
    private val FISHING_XP_REGEX = """\+([0-9,]+(?:\.[0-9]+)?) Fishing""".toRegex()

    var startFishing: Instant = Instant.DISTANT_PAST
        private set

    var lastFishingEvent: Instant = Instant.DISTANT_PAST
        private set

    val isFishing: Boolean
        get() = startFishing != Instant.DISTANT_PAST

    override fun onInitialize() {
        registerGameEvent(FISHING_XP_REGEX, isOverlay = true) { _, _, _ ->
            updateFishingStatus()
        }

        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            updateFishingStatus()
        }

        registerTickEvent(interval = 20) {
            val now = Clock.System.now()
            val limit = GeneralFishing.fishingTime.minutes

            if (isFishing && (now - lastFishingEvent) > limit) {
                resetSession()
            }
        }
    }

    private fun updateFishingStatus() {
        val now = Clock.System.now()
        if (startFishing == Instant.DISTANT_PAST) {
            startFishing = now
        }
        lastFishingEvent = now
    }

    fun resetSession() {
        startFishing = Instant.DISTANT_PAST
        lastFishingEvent = Instant.DISTANT_PAST

        SeaCreatureHour.resetSession()
        SeaCreatureHour.updateRate()

        InkSessionTracker.resetSession()
        InkSessionTracker.updateRate()

        FishingXpTracker.resetSession()
        FishingXpTracker.updateRate()
    }

    @Command
    object ResetCommand : SimpleCommand("rfuresetsession") {
        override val description: String = "Resets your current fishing session."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            resetSession()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("Your fishing session has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }
}
