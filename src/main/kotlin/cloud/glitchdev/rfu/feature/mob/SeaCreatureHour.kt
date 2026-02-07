package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.SCHDisplay
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@RFUFeature
object SeaCreatureHour : Feature {
    private val catchHistory = ArrayDeque<Instant>()

    var currentScPerHour: Double = 0.0
        private set

    var lastSC: Instant = Instant.DISTANT_PAST
    var startFishing: Instant = Instant.DISTANT_PAST
    var total: Int = 0

    override fun onInitialize() {
        registerSeaCreatureCatchEvent {
            handleCatch()
        }

        registerTickEvent(interval = 20) {
            updateRate()
        }

        Command.registerCommand("rfuresetsch") { context ->
            currentScPerHour = 0.0
            lastSC = Instant.DISTANT_PAST
            startFishing = Instant.DISTANT_PAST
            total = 0
            catchHistory.clear()
            updateRate()

            context.source.sendFeedback(TextUtils.rfuLiteral("The SC/h tracker has been reset!", TextStyle(TextColor.LIGHT_GREEN)))

            1
        }
    }

    private fun handleCatch() {
        val now = Clock.System.now()

        if (startFishing == Instant.DISTANT_PAST) {
            startFishing = now
        }

        lastSC = now
        total++
        catchHistory.add(now)

        updateRate()
    }

    private fun updateRate() {
        val now = Clock.System.now()
        val limit = GeneralFishing.fishingTime.minutes

        if (lastSC != Instant.DISTANT_PAST && (now - lastSC) > limit) {
            resetSession()
        }

        if (startFishing == Instant.DISTANT_PAST) {
            currentScPerHour = 0.0
            SCHDisplay.updateData(0, 0, Duration.ZERO)
            return
        }

        while (catchHistory.isNotEmpty() && (now - catchHistory.first()) > limit) {
            catchHistory.removeFirst()
        }

        val timeElapsed = now - startFishing
        val calculationWindow = if (timeElapsed < limit) timeElapsed else limit

        if (calculationWindow.inWholeSeconds == 0L) {
            currentScPerHour = 0.0
            SCHDisplay.updateData(0, total, timeElapsed)
            return
        }

        currentScPerHour = (catchHistory.size.toDouble() / calculationWindow.inWholeSeconds) * 3600
        SCHDisplay.updateData(currentScPerHour.toInt(), total, timeElapsed)
    }

    private fun resetSession() {
        startFishing = Instant.DISTANT_PAST
        lastSC = Instant.DISTANT_PAST
        total = 0
        catchHistory.clear()
        currentScPerHour = 0.0
    }
}