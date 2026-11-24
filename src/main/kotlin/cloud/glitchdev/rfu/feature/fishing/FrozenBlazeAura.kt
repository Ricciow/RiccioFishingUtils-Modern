package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.inventory.ArmorEvents
import cloud.glitchdev.rfu.events.managers.inventory.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Tracks the Frozen Blazing Aura (full Frozen Blaze set bonus).
 * The aura requires BOTH the mouse/camera and the player body to have moved
 * within the last [TIMEOUT]. Moving only the camera lets the body timer
 * expire (and vice-versa). [remainingDuration] is the shorter of the two timers.
 */
@RFUFeature
object FrozenBlazeAura : Feature {
    private val TIMEOUT = 30.seconds

    var hasFrozenBlazeArmor = false
        private set

    private var lastMouseActivity = TimeSource.Monotonic.markNow()
    private var lastPlayerActivity = TimeSource.Monotonic.markNow()

    private var lastX = Double.NaN
    private var lastY = Double.NaN
    private var lastZ = Double.NaN
    private var lastYaw = Float.NaN
    private var lastPitch = Float.NaN

    val remainingDuration: Duration
        get() = (TIMEOUT - maxOf(lastMouseActivity.elapsedNow(), lastPlayerActivity.elapsedNow()))
            .coerceIn(Duration.ZERO, TIMEOUT)

    override fun onInitialize(scope: EventScope) {
        scope.registerArmorChangeEvent { updateArmor() }
        scope.registerTickEvent(interval = 5) {
            val player = mc.player

            if (player == null) {
                lastX = Double.NaN
                lastY = Double.NaN
                lastZ = Double.NaN
                lastYaw = Float.NaN
                lastPitch = Float.NaN
                return@registerTickEvent
            }

            val now = TimeSource.Monotonic.markNow()
            val mouseMoved = !lastYaw.isNaN() && !lastPitch.isNaN() &&
                    (player.yRot != lastYaw || player.xRot != lastPitch)

            val playerMoved = !lastX.isNaN() && !lastY.isNaN() && !lastZ.isNaN() &&
                    (player.x != lastX || player.y != lastY || player.z != lastZ)

            lastYaw = player.yRot
            lastPitch = player.xRot
            lastX = player.x
            lastY = player.y
            lastZ = player.z

            if (mouseMoved) lastMouseActivity = now
            if (playerMoved) lastPlayerActivity = now
        }
        updateArmor()
    }

    private fun updateArmor() {
        val wearing = ArmorEvents.currentArmorSet.isWearingFrozenBlazeSet
        if (wearing && !hasFrozenBlazeArmor) refreshTimers()
        hasFrozenBlazeArmor = wearing
    }

    private fun refreshTimers() {
        val now = TimeSource.Monotonic.markNow()
        lastMouseActivity = now
        lastPlayerActivity = now
    }
}
