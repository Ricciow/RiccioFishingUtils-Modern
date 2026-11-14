package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.ArmorSet
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import kotlin.math.abs
import kotlin.math.min

/**
 * Tracks the Frozen Blazing Aura (full Frozen Blaze set bonus).
 * The aura requires BOTH the mouse/camera and the player body to have moved
 * within the last [TIMEOUT_MS]. Moving only the camera lets the body timer
 * expire (and vice-versa). [remainingMs] is the shorter of the two timers.
 */
@RFUFeature
object FrozenBlazeAura : Feature {
    const val TIMEOUT_MS = 30_000L
    private const val POS_EPSILON = 1.0E-4
    private const val ROT_EPSILON = 0.01f

    var hasFrozenBlazeArmor = false
        private set

    private var lastMouseActiveMs = System.currentTimeMillis()
    private var lastPlayerActiveMs = System.currentTimeMillis()

    private var lastX = Double.NaN
    private var lastY = Double.NaN
    private var lastZ = Double.NaN
    private var lastYaw = Float.NaN
    private var lastPitch = Float.NaN

    private val mouseRemainingMs: Long
        get() = (TIMEOUT_MS - (System.currentTimeMillis() - lastMouseActiveMs)).coerceIn(0L, TIMEOUT_MS)

    private val playerRemainingMs: Long
        get() = (TIMEOUT_MS - (System.currentTimeMillis() - lastPlayerActiveMs)).coerceIn(0L, TIMEOUT_MS)

    val remainingMs: Long
        get() = min(mouseRemainingMs, playerRemainingMs)

    val isAuraActive: Boolean
        get() = hasFrozenBlazeArmor && remainingMs > 0L

    override fun onInitialize() {
        registerArmorChangeEvent { updateArmor() }
        registerTickEvent { onTick() }
        updateArmor()
    }

    private fun updateArmor() {
        val wearing = ArmorEvents.currentArmorSet.isWearingFrozenBlazeSet
        // Freshly equipped set starts with a full timer.
        if (wearing && !hasFrozenBlazeArmor) refreshTimers()
        hasFrozenBlazeArmor = wearing
    }

    private fun onTick() {
        val player = mc.player
        if (player == null) {
            // Reset position/rotation baselines so the first tick after rejoining
            // doesn't count as "movement", but do NOT refresh timers — the aura
            // should not stay active just because the player object is momentarily null.
            lastX = Double.NaN
            lastY = Double.NaN
            lastZ = Double.NaN
            lastYaw = Float.NaN
            lastPitch = Float.NaN
            return
        }

        // Poll directly from the player's equipment slots every tick.
        // This bypasses ArmorEvents.currentArmorSet (which only updates on SetSlot
        // packets and can be stale if the debounce dropped an unequip event).
        val wearing = ArmorSet.fromPlayer(player).isWearingFrozenBlazeSet
        if (wearing && !hasFrozenBlazeArmor) refreshTimers()
        hasFrozenBlazeArmor = wearing

        val now = System.currentTimeMillis()

        // NaN baseline = first tick after joining; just record it, don't count as movement.
        val mouseMoved = !lastYaw.isNaN() && !lastPitch.isNaN() &&
                (abs(player.yRot - lastYaw) > ROT_EPSILON ||
                        abs(player.xRot - lastPitch) > ROT_EPSILON)

        val playerMoved = !lastX.isNaN() && !lastY.isNaN() && !lastZ.isNaN() &&
                (abs(player.x - lastX) > POS_EPSILON ||
                        abs(player.y - lastY) > POS_EPSILON ||
                        abs(player.z - lastZ) > POS_EPSILON)

        lastYaw = player.yRot
        lastPitch = player.xRot
        lastX = player.x
        lastY = player.y
        lastZ = player.z

        if (mouseMoved) lastMouseActiveMs = now
        if (playerMoved) lastPlayerActiveMs = now
    }

    private fun refreshTimers() {
        val now = System.currentTimeMillis()
        lastMouseActiveMs = now
        lastPlayerActiveMs = now
    }
}
