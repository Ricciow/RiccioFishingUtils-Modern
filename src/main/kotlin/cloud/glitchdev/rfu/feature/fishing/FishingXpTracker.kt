package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.XPHDisplay
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@RFUFeature
object FishingXpTracker : Feature {
    val FISHING_XP_REGEX = """\+([0-9,]+(?:\.[0-9]+)?) Fishing""".toRegex()

    private val xpHistory = ArrayDeque<Pair<Instant, Double>>()
    private var lastSeenXp: String = ""
    private var lastXpEvent: Instant = Instant.DISTANT_PAST
    private var startFishing: Instant = Instant.DISTANT_PAST

    var totalXp: Double = 0.0
        private set

    var currentXpPerHour: Double = 0.0
        private set

    override fun onInitialize() {
        registerGameEvent(FISHING_XP_REGEX, isOverlay = true) { _, _, matches ->
            val xpStr = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            if (xpStr == lastSeenXp) return@registerGameEvent
            lastSeenXp = xpStr
            val xp = xpStr.replace(",", "").toDoubleOrNull() ?: return@registerGameEvent
            if (xp <= 0) return@registerGameEvent
            handleXpGain(xp)
        }

        registerTickEvent(interval = 20) {
            updateRate()
        }
    }

    @Command
    object ResetCommand : SimpleCommand("rfuresetxph") {
        override val description: String = "Resets your current Xp/h tracker."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            resetSession()
            updateRate()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("The Xp/h tracker has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }

    private fun handleXpGain(xp: Double) {
        val now = Clock.System.now()

        if (startFishing == Instant.DISTANT_PAST) {
            startFishing = now
        }

        lastXpEvent = now
        totalXp += xp
        xpHistory.add(Pair(now, xp))

        updateRate()
    }

    private fun updateRate() {
        val now = Clock.System.now()
        val limit = GeneralFishing.fishingTime.minutes

        if (lastXpEvent != Instant.DISTANT_PAST && (now - lastXpEvent) > limit) {
            resetSession()
        }

        if (startFishing == Instant.DISTANT_PAST) {
            currentXpPerHour = 0.0
            XPHDisplay.updateData(0L, 0.0, Duration.ZERO)
            return
        }

        while (xpHistory.isNotEmpty() && (now - xpHistory.first().first) > limit) {
            xpHistory.removeFirst()
        }

        val timeElapsed = now - startFishing
        val calculationWindow = if (timeElapsed < limit) timeElapsed else limit

        if (calculationWindow.inWholeSeconds == 0L) {
            currentXpPerHour = 0.0
            XPHDisplay.updateData(0L, totalXp, timeElapsed)
            return
        }

        val windowXp = xpHistory.sumOf { it.second }
        currentXpPerHour = (windowXp / calculationWindow.inWholeSeconds) * 3600
        XPHDisplay.updateData(currentXpPerHour.toLong(), totalXp, timeElapsed)
    }

    private fun resetSession() {
        startFishing = Instant.DISTANT_PAST
        lastXpEvent = Instant.DISTANT_PAST
        lastSeenXp = ""
        xpHistory.clear()
        totalXp = 0.0
        currentXpPerHour = 0.0
    }
}